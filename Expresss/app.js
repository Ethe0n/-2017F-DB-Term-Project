var net = require('net');

// tcp/ip server
var server = net.createServer(function(socket) {
    console.log(socket.address().address + " connected.");

    socket.on('data', function(data){
        console.log('client disconnected..');
    });

    socket.write('welcome to server');
});

server.on('error', function(err){
    console.log('err' + err);
});

server.listen(5000, function() {
    console.log('socket server> listening on port 5000...');
});

// http server
var express = require('express');
var app = express();
var path = require('path');
var bodyParser = require('body-parser');

app.locals.pretty = true;

app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));

app.use(express.static('public'));
app.use(bodyParser.urlencoded({ extended: false }));

app.get('/', function(req, res) {
    res.render('index');
})
app.post('/result', function(req, res) {
    res.render('result', {
        name: req.body.name,
        year: req.body.year
    });
});

app.listen(8080, function() {
    console.log('http server> listening on port 8080...');
});