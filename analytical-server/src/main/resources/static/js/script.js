'use strict';

var welcomeForm = document.querySelector('#welcomeForm');
var dialogueForm = document.querySelector('#dialogueForm');
welcomeForm.addEventListener('submit', connect, true)

var stompClient = null;
var name = null;

function connect(event) {
    name = document.querySelector('#name').value.trim();

    if (name) {
        document.querySelector('#welcome-page').classList.add('hidden');
        document.querySelector('#dialogue-page').classList.remove('hidden');

        var socket = new SockJS('/analytical-server');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, connectionSuccess);
    }
    event.preventDefault();
}

function connectionSuccess() {
    stompClient.subscribe('/topic/ohlc_notify', onMessageReceived);

    stompClient.send("/app/as.newUser", {}, JSON.stringify({
        symbol: name,
        event: 'subscribe',
        interval: "15"
    }))
}

function onMessageReceived(payload) {
    var messageElement = document.createElement('li');
    var textElement = document.createElement('p');
    var messageText = document.createTextNode(payload.body);

    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    document.querySelector('#messageList').appendChild(messageElement);
    document.querySelector('#messageList').scrollTop = document
        .querySelector('#messageList').scrollHeight;
}
