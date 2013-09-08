
window.socket = new WebSocket(window.location.href.replace("http://", "ws://").replace("qr-wrapper", "ws"));

socket.onopen = function() {
    return console.log("socket opened");
};

socket.onmessage = function(msg) {
    return $("#message").replaceWith("<h1 id=\"message\">" + msg.data + "</h1>");
};

