angular.module('webChat', [])
    .controller('ChatController', function ChatController($scope) {
        $scope.contacts = [];
        var stompClient,
            connect = function () {
                var socket = new SockJS('/chat');
                stompClient = Stomp.over(socket);
                stompClient.connect({}, function (frame) {
                    stompClient.subscribe('/user/topic/contacts', function (response) {
                        $scope.contacts = JSON.parse(response.body);
                        $scope.$digest();
                    });
                    stompClient.send("/app/get-contacts", {});
                });
            };
        $scope.addContact = function () {
            console.log($scope.contacts);
        };
        connect(); // Start the web-socket connection
    });