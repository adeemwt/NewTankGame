var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
app.get('/',function(req,res){
res.sendFile(_dirname+'/index.html');
})
io.on('connection', function(socket){
    console.log('no user found')
})

http.listen(3000,function(){
    console.log('server listening on port 3000');
})