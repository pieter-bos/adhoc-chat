var chatApp = angular.module('chatApp', []);
var socket = new WebSocket('ws://localhost:8081/');

var emoticons = [{code: "(o^.^o)", link: "../chat-emoticons/emoticon-1.gif"},
                {code: "(=@=)", link: "../chat-emoticons/emoticon-2.gif"},
                {code: "(^o^)", link: "../chat-emoticons/emoticon-3.gif"},
                {code: "(\\-.-)", link: "../chat-emoticons/emoticon-4.gif"},
                {code: "(nod)", link: "../chat-emoticons/emoticon-5.gif"},
                {code: "(\\^.^/)", link: "../chat-emoticons/emoticon-6.gif"},
                {code: "(-.-)", link: "../chat-emoticons/emoticon-7.gif"},
                {code: "(^o^\\)", link: "../chat-emoticons/emoticon-8.gif"},
                {code: "(O.)", link: "../chat-emoticons/emoticon-9.gif"},
                {code: "(o^o)", link: "../chat-emoticons/emoticon-10.gif"},
                {code: "(\\,/)", link: "../chat-emoticons/emoticon-11.gif"},
                {code: "(=_=)", link: "../chat-emoticons/emoticon-12.gif"},
                {code: "(^.^)", link: "../chat-emoticons/emoticon-13.gif"},
                {code: "(T0T)", link: "../chat-emoticons/emoticon-14.gif"},
                {code: "(\\^o^)", link: "../chat-emoticons/emoticon-15.gif"}];

function Conversation() {
    this.id = Math.floor((Math.random()*100000)+1);
    this.members = [];
    this.message = "";
    this.messages = [];

    this.title = function() {
        return this.id;
    }

    this.addMember = function(user) {
        this.members.push(user);
        console.log(this.id);
        console.log(this.members);
    }
}

chatApp.controller('userController', function ($scope, $rootScope) {
    $scope.nick = '';

    $scope.users = [
        {name: "Laurens"},
        {name: "Sophie"}
    ];

    $scope.addToConversation = function() {
        $rootScope.$emit('addMember', this.user);
        console.log('emit');
    }

    angular.element(document).ready(function() {
        var nick = "";
        while (nick == "") {
            nick = prompt("What would you like your chat name to be?");
            var i=0;
            while (i<$scope.users.length) {
                if (nick == $scope.users[i].name){
                    nick="";
                    break;
                }
                i++;
            }
            if (nick != ""  && nick != "you" && nick!="You" && nick!=null) {
                $scope.nick = nick;
            } else {
                nick = "";
            };
        }
    });
});

chatApp.controller('conversationController', function($scope, $rootScope) {
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

    $rootScope.$on('addMember', function(e, user) {
        $scope.active.addMember(user);
        socket.send({conversation: $scope.active.id, member: user.name});
    });

    $scope.sendMessage = function() {
        if (this.conversation.message != '') {
            socket.send({type: 'text', conversation: this.conversation.id, text: this.conversation.message});
            console.log(this.conversation.message);
            this.conversation.messages.push({name: 'you', text: this.conversation.message});
            this.conversation.message = "";
            $(".message-field").animate({scrollTop: $(document).height()+999999}, "slow");
        }
    }

    $scope.isActive = function(conversation) {
        return $scope.active == conversation ? 'active' : '';
    }

    $scope.leaveConversation = function() {
        var conv = $scope.conversations.indexOf(this.conversation);
        $scope.conversations.splice(conv, conv+1);
        $scope.active = $scope.conversations[0];
        socket.send({type: "leave", conversation: this.conversation.id});
    }
});
