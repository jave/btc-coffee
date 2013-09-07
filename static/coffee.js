
window.socket = new WebSocket(
                              "ws://localhost:9198/ws"
                              //window.location.href.replace("http://", "ws://")
                              );

socket.onopen = function() {
    return console.log("socket opened");
};

socket.onmessage = function(msg) {
    return $("#message").replaceWith("<h1 id=\"message\">" + msg.data + "</h1>");
};

