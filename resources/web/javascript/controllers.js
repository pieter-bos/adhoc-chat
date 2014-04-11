var chatApp = angular.module("chatApp", []);

chatApp.service("websocketService", function () {
    return new WebSocketJSONRPC("ws://localhost:8081/").def("send").def("leave").def("invite").def("nick");
});

function Conversation() {
    this.id = Math.floor((Math.random()*100000)+1);
    this.members = [];
    this.message = "";
    this.messages = [{name: "Welcome", text: "Don't be an asshole!"}];

    this.title = function() {
        return this.id;
    }

    this.addMember = function(user) {
        this.members.push(user);
    }
}

chatApp.controller("userController", function ($scope, $rootScope, websocketService) {
    $scope.nick = "";
    $scope.users = [];

    $scope.addToConversation = function() {
        $rootScope.$emit("addMember", this.user);
        console.log("emit");
    }

    $scope.showModal = function() {
        $('#nick-modal').modal();
    }

    angular.element(document).ready(function() {
        $('#nick-modal').modal({ keyboard: false, backdrop: 'static' });
        $('#nick-modal form').on('submit', function(event) {
            $('#nick-modal').modal('hide');
        });
    });
});

chatApp.controller("conversationController", function($scope, $rootScope, websocketService) {
    $scope.conversations = [
        new Conversation()
    ];

    $scope.EMOTICONS = [
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
        { code: "(\\^o^)", link: "chat-emoticons/emoticon-15.gif "}
    ];

    $scope.active = $scope.conversations[0];

    $scope.addConversation = function() {
        var conv = new Conversation();
        $scope.active = conv;
        $scope.conversations.push(conv);
    }

    $scope.setActive = function() {
        $scope.active = this.conversation;
    }

    $rootScope.$on("addMember", function(e, user) {
        $scope.active.addMember(user);
        websocketService.invite($scope.active.id, user);
    });

    $rootScope.$on("addEmoticon", function(e, emoticon) {
        $scope.active.message += emoticon;
    });

    $scope.parseMessage = function(message) {
        var set = new Set();

        for (var i in $scope.EMOTICONS) {
            var emoticon = $scope.EMOTICONS[i];

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

        console.log(indices);

        var result = [];

        for(var i = 0; i < indices.length - 1; i++) {
            var left = indices[i];
            var right = indices[i+1];

            var str = message.substring(left, right);

            var emoticon = $scope.getEmoticon(str);

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

        console.log(result);

        return result;
    }

    $scope.getEmoticon = function(string) {
        for (var j=0 ; j<$scope.EMOTICONS.length; j++) {
            if ($scope.EMOTICONS[j].code === string) {
                return $scope.EMOTICONS[j];
            }
        }
        return null;
    }

    $scope.sendMessage = function() {
        if (this.conversation.message != "") {
            this.conversation.messages.push({name: "you", value: $scope.parseMessage(this.conversation.message)});
            this.conversation.message = "";
            $(".tab-pane").animate({scrollTop: $(".tab-pane").height()+999999}, "slow");

            for (member in this.conversation.members) {
                websocketService.send(this.conversation.id, this.conversation.message, member);
            }
        }
    }

    $scope.isActive = function(conversation) {
        return $scope.active == conversation ? "active" : "";
    }

    $scope.leaveConversation = function() {
        var conv = $scope.conversations.indexOf(this.conversation);
        for (member in $scope.conversations[conv].members) {
            websocketService.leave(conv.id, member, function(result) { console.log(result); });
        }

        $scope.conversations.splice(conv, 1);
        $scope.active = $scope.conversations[0];
    }
});

chatApp.controller("emoticonController", function($scope, $rootScope, websocketService) {
    $scope.addEmoticon = function(e) {
            $rootScope.$emit("addEmoticon", $scope.emoticon.code);
    }
});
