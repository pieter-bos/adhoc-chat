var chatApp = angular.module("chatApp", []);

chatApp.service("websocketService", function () {
    return new WebSocketJSONRPC("ws://localhost:8081/").def("send").def("leave").def("invite").def("nick");
});

const EMOTICONS = [
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
        console.log(this.id);
        console.log(this.members);
    }
}

chatApp.controller("userController", function ($scope, $rootScope, websocketService) {
    $scope.nick = "";

    $scope.users = [
        {name: "Laurens"},
        {name: "Sophie"}
    ];

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
        socket.send({conversation: $scope.active.id, member: user.name});
    });

    $scope.sendMessage = function() {
        if (this.conversation.message != "") {
            this.conversation.messages.push({name: "you", text: this.conversation.message});
            this.conversation.message = "";
            $(".tab-pane").animate({scrollTop: $(".tab-pane").height()+999999}, "slow");
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

        $scope.conversations.splice(conv, conv+1);
        $scope.active = $scope.conversations[0];
    }
});
