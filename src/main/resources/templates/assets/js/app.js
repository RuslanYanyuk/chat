angular.module('webChat', ['ui.bootstrap'])
    .controller('ChatController', function ChatController($scope, $uibModal, chat) {
        $scope.data = chat;
        $scope.nameFilter = '';

        $scope.openModalAddContact = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                templateUrl: 'assets/fragments/add-contact-modal.html',
                controller: 'AddContactModalController'
            })
        };
    })
    .controller('AddContactModalController', function ($scope, $uibModalInstance, chat) {
        $scope.selectedIndex = -1;
        $scope.searchQuery = '';
        $scope.data = chat;

        $scope.setSelected = function (v) {
            $scope.selectedIndex = v;
        };

        $scope.addContact = function () {
            chat.stompClient.send("/app/add-contact", {},
                JSON.stringify({
                    name: chat.foundUsers[$scope.selectedIndex].name
                })
            );
            $uibModalInstance.close();
        };

        $scope.$watch('searchQuery', function () {
            chat.stompClient.send("/app/find-users", {},
                JSON.stringify({
                    name: $scope.searchQuery
                })
            );
        });
    })
    .service('chat', ['$rootScope', function ($rootScope) {
        var socket = new SockJS('/chat'),
            stompClient = Stomp.over(socket),
            username,

            connect = function () {
                stompClient.connect({}, function (frame) {
                    username = frame.headers["user-name"];
                    stompClient.subscribe('/user/topic/chat-rooms', function (response) {
                        var chatRoomsRaw = JSON.parse(response.body);
                        for (var i = 0; i < chatRoomsRaw.length; i++) {
                            var participants = chatRoomsRaw[i].participants;
                            var room = {
                                topic: '',
                                participant: {}
                            };
                            for (var j = 0; j < participants.length; j++) {
                                if (participants[j].name == username) {
                                    continue;
                                }
                                room.participant = participants[j];
                            }
                            room.topic = chatRoomsRaw[i].topic;
                            _data.chatRooms.push(room);
                        }
                        $rootScope.$digest();
                    });
                    stompClient.subscribe('/user/topic/found-users', function (response) {
                        _data.foundUsers = JSON.parse(response.body);
                        $rootScope.$digest();
                    });
                    stompClient.send("/app/get-chat-rooms", {});
                });
            },

            _data = {
                foundUsers: [],
                stompClient: stompClient,
                chatRooms: []
            };
        connect();

        return _data;
    }]);