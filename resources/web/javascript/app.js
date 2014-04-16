// Conversation object
function Conversation(user, removable) {
    this.id = Math.floor((Math.random()*100000)+1);
    this.user = user;
    this.active = 'active';
    this.removable = removable == undefined ? true : removable;

    this.message = '';
    this.messages = [];

    this.title = function() {
        return this.user;
    }

    this.equals = function(other) {
        return this.user === other.user;
    }
}

var chat = angular.module('chat', [])
// Provides functions for websocket communication
.service('websocketService', function($rootScope) {
    var self = this;
    this.socket = new WebSocketJSONRPC("ws://localhost:8081/")
        .def('getConversations')
        .def('getUsers')
        .def('updateNickname')
        .def('sendMessage')
        .def('subscribe')
        .def('addConversation')
        .def('leaveConversation');


    this.updateNickname = function(nickname) {
        this.socket.updateNickname(nickname, function(e) {
            if (e != '') {
                $rootScope.$broadcast('websocketService::nickChanged', e);
            }
        });
    }

    this.sendMessage = function(message, convId) {
        this.socket.sendMessage(message, convId, function(e) {
            var m = JSON.parse(e);
            $rootScope.$broadcast('websocketService::newMessage', m.convId, m.message, m.nickname);
        });
    }

    this.addConversation = function(conversation) {
        this.socket.addConversation(conversation.user, function() {});
    }

    this.leaveConversation = function(conv) {
        this.socket.leaveConversation(conv.id, function() {});
    }

    this.socket.on('newConversation', function(user, messages, id) {
        $rootScope.$broadcast('websocketService::newConversation', { user: user, messages: messages, id: id });
    });

    this.socket.on('newUser', function(user) {
        $rootScope.$broadcast('websocketService::newUser', user);
    });

    this.socket.on('removeUser', function(user) {
        $rootScope.$broadcast('websocketService::removeUser', user);
    });

    this.socket.on('newMessage', function(conv, user, message) {
        $rootScope.$broadcast('websocketService::newMessage', conv, message, user);
    });

    this.socket.on('leaveConversation', function(conv) {
        $rootScope.$broadcast('websocketService::leaveConversation', conv);
    });

    this.socket.on('error', function(error) {
        // TODO implement
    });

    this.socket.on('open', (function() {
        this.socket.subscribe('newConversation', function() {});
        this.socket.subscribe('leaveConversation', function() {});

        this.socket.subscribe('newUser', function() {});
        this.socket.subscribe('removeUser', function() {});

        this.socket.subscribe('newMessage', function() {});

        $rootScope.$broadcast('websocketService::connected');

        self.socket.getConversations(function(e) {
            var arr = JSON.parse(e);
            arr.forEach(function(element) {
                $rootScope.$broadcast('websocketService::newConversation', element);
            });
        });

        self.socket.getUsers(function(e) {
            JSON.parse(e).forEach(function(element) {
                $rootScope.$broadcast('websocketService::newUser', element);
            });
        });
    }).bind(this));
})
// Provides application settings
.service('settingService', function($rootScope, websocketService) {
    this.nickname = 'choose a nickname...';

    this.updateNickname = function(nickname) {
        websocketService.updateNickname(nickname);
    }

    $rootScope.$on('websocketService::nickChanged', function(event, nick) {
        this.nickname = nick;
        $rootScope.$broadcast('settingService::nicknameChanged');
    });

    this.init = function() {
        $('#nick-modal').modal({ keyboard: false, backdrop: 'static' });
    }

    this.init();
})
// Provides functions to manipulate user model
.service('userModel', function($rootScope, websocketService) {
    var self = this;
    this.users = [];

    $rootScope.$on('websocketService::newUser', function(event, user) {
        self.users.push(user);
        $rootScope.$broadcast('userModel::usersChanged');
    });

    $rootScope.$on('websocketService::removeUser', function(event, user) {
        self.users.splice(self.users.indexOf(user), 1);
        $rootScope.$broadcast('userModel::usersChanged');
    });
})
// Provides function to manipulate conversation model
.service('conversationModel', function($rootScope, websocketService) {
    var self = this;
    this.conversations = [];

    this.EMOTICONS = [
         { code: "(o^.^o)", link: "chat-emoticons/emoticon-1.gif" },
         { code: "(=@=)", link: "chat-emoticons/emoticon-2.gif" },
         { code: "(^o^)", link: "chat-emoticons/emoticon-3.gif" },
         { code: "(\\-.-)", link: "chat-emoticons/emoticon-4.gif" },
         { code: "(nod)", link: "chat-emoticons/emoticon-5.gif" },
         { code: "(\\^.^/)", link: "chat-emoticons/emoticon-6.gif" },
         { code: "(-.-)", link: "chat-emoticons/emoticon-7.gif" },
         { code: "(^o^\\)", link: "chat-emoticons/emoticon-8.gif" },
         { code: "(O.)", link: "chat-emoticons/emoticon-9.gif" },
         { code: "(o^o)", link: "chat-emoticons/emoticon-10.gif" },
         { code: "(\\,/)", link: "chat-emoticons/emoticon-11.gif" },
         { code: "(=_=)", link: "chat-emoticons/emoticon-12.gif" },
         { code: "(^.^)", link: "chat-emoticons/emoticon-13.gif" },
         { code: "(T0T)", link: "chat-emoticons/emoticon-14.gif" },
         { code: "(\\^o^)", link: "chat-emoticons/emoticon-15.gif"}
     ];

    // Adds a new conversation to the list and makes all other conversations inactive
    this.addConversation = function(conv) {
        for (var i = 0; i < this.conversations.length; i++) {
            this.conversations[i].active = '';
        }

        this.conversations.push(conv);
    }

    // Removes a conversation and makes the default conversation active
    this.removeConversation = function(conv) {
        for (var id in this.conversations) {
            if (this.conversations[id].id == conv) {
                this.conversations.splice(id, 1);
            }
        }

        this.conversations[0].active = 'active';

        $rootScope.$broadcast('conversationModel::conversationsChanged');
    }

    // Starts a new conversation with the user if there is no current conversation with the user
    this.startConversation = function(user) {
        var newConv = new Conversation(user);

        for (var i = 0; i < this.conversations.length; i++) {
            if (this.conversations[i].equals(newConv)) {
                return;
            }
        }

        websocketService.addConversation(newConv);
    }

    $rootScope.$on('websocketService::newMessage', function(event, id, message, nickname) {
        for (var i = 0; i < self.conversations.length; i++) {
            if (self.conversations[i].id === id) {
                self.conversations[i].messages.push(
                    {name: nickname, value: self.parseMessage(message)}
                );
                $(".tab-pane").animate({scrollTop: $(".tab-pane").height()+999999}, "slow");
            }
        }

        $rootScope.$broadcast('conversationModel::conversationsChanged');
    });

    $rootScope.$on('websocketService::newConversation', function(event, data) {
        var removable = data.user === '' ? false : true;
        var user = data.user === '' ? 'Everyone' : data.user;
        var conv = new Conversation(user, removable);
        conv.id = data.id;
        conv.messages = data.messages;
        self.addConversation(conv);

        $rootScope.$broadcast('conversationModel::conversationsChanged');
    });

    $rootScope.$on('websocketService::leaveConversation', function(event, convId) {
        self.removeConversation(convId);
    });

    this.parseMessage = (function(message) {
            var set = new Set();

            for (var i in this.EMOTICONS) {
                var emoticon = this.EMOTICONS[i];

                var lastIndex = -1;

                while(true) {
                    var part = message.substring(lastIndex + 1);
                    var subIndex = part.indexOf(emoticon.code);

                    if(subIndex === -1) {
                        break;
                    }

                    lastIndex = subIndex + lastIndex + 1;
                    set.add(lastIndex);
                    set.add(lastIndex + emoticon.code.length);
                }
            }

            set.add(0);
            set.add(message.length);

            var indices = [ index for (index of set) ];
            indices.sort(function(a, b) { return (a > b) - (a < b); });

            var result = [];

            for(var i = 0; i < indices.length - 1; i++) {
                var left = indices[i];
                var right = indices[i+1];

                var str = message.substring(left, right);

                var emoticon = this.getEmoticon(str);

                if (emoticon) {
                    result.push({
                        type: "emoticon",
                        src: emoticon.link
                    });
                } else {
                    result.push({
                        type: "text",
                        value: str
                    });
                }

            }

            return result;
        }).bind(this);

        this.getEmoticon = (function(string) {
            for (var j=0 ; j<this.EMOTICONS.length; j++) {
                if (this.EMOTICONS[j].code === string) {
                    return this.EMOTICONS[j];
                }
            }
            return null;
        }).bind(this);
})
// Controller for application settings
.controller('settingController', function($scope, settingService) {
    $scope.connected = false;
    $scope.nickname = settingService.nickname;

    $scope.nicknameField = '';

    $scope.updateNickname = function() {
        settingService.updateNickname($scope.nicknameField);
        $scope.nicknameField = '';
    }

    $scope.$on('settingService::nicknameChanged', function() {
        $('#nick-modal').modal('hide');
        $scope.nickname = nickname;
        $scope.$apply();
    });

    $scope.$on('websocketService::connected', function() {
        $scope.connected = true;
        $scope.$apply();
    });
})
// Controller for user related views
.controller('userController', function($scope, userModel, conversationModel) {
    $scope.users = userModel.users;

    $scope.startConversation = function() {
        conversationModel.startConversation(this.user);
    }

    $scope.$on('userModel::usersChanged', function() {
        $scope.users = userModel.users;
        $scope.$apply();
    });
})
// Controller for conversation related views
.controller('conversationController', function($scope, conversationModel, websocketService) {
    $scope.EMOTICONS = conversationModel.EMOTICONS;

    $scope.conversations = conversationModel.conversations;

    $scope.$on("addEmoticon", function(e, emoticon) {

    });

    $scope.sendMessage = function() {
        if (this.conversation.message != '') {
            websocketService.sendMessage(this.conversation.message, this.conversation.id);
            this.conversation.message = '';
        }
    }

    $scope.closeConversation = function() {
        conversationModel.removeConversation(this.conversation);
        websocketService.leaveConversation(this.conversation);
        $scope.$emit('conversationModel::conversationsChanged');
    }

    $scope.$on('conversationModel::conversationsChanged', function() {
        $scope.conversations = conversationModel.conversations;
        $scope.$apply();
    });

    $scope.addEmoticon = function(e) {
        this.conversation.message += this.emoticon.code;
    }
});
