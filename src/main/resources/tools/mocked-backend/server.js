/**
 * Dev server
 */
var connect = require('connect'),
    serveStatic = require('serve-static'),
    proxy = require('proxy-middleware'),
    fs = require('fs'),
    sass = require('node-sass'),
    url = require('url'),
    PORT = 9000;

var sassInput = "../../templates/assets/scss/main.scss",
    sassOutput = "../../templates/assets/css/main.css";

var app = connect();

function respondWithPage(response, page) {
    fs.readFile('../../templates/' + page + '.html', function (err, html) {
        if (err) {
            throw err;
        }
        response.writeHeader(200, {"Content-Type": "text/html; charset=utf-8"});
        response.write(html);
        response.end();
    });
}

app.use(function barMiddleware(req, response, next) {
    if (req.url.startsWith('/assets/css')) {
        compileSass();
        next();
        return
    }
    if (req.url.startsWith('/assets')) {
        next();
        return
    }

    switch (req.url) {
        case '/' :
            respondWithPage(response, 'main');
            break;
        case '/login' :
        case '/logout' :
            respondWithPage(response, 'login');
            break;
        case '/registration' :
            respondWithPage(response, 'registration');
            break;
        default :
            respondWithPage(response, 'main');
    }
});

app.use(serveStatic("../../templates/"), {index: false}).listen(PORT, function () {
    console.log("Server listening on: http://localhost:%s", PORT);
});

function compileSass() {
    sass.render({
        file: sassInput,
        includePaths: ['../../templates/assets/',
        '../../templates/assets/scss/bourbon/']
    }, function (err, result) {
        if (err != null) {
            console.log('Error occurred: ' + err);
            return
        }
        fs.writeFileSync(sassOutput, result.css);
    });
}