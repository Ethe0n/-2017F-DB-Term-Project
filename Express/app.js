var net = require('net');
var rcvData;

function getConnection(connName) {
    var client = net.connect({ port: 5000, host: '127.0.0.1' }, function () {
        console.log(connName + ' Connected: ');
        console.log('   local = %s:%s', this.localAddress, this.localPort);
        console.log('   remote = %s:%s', this.remoteAddress, this.remotePort);
        this.setEncoding('utf8');

        this.on('data', function (data) {
            rcvData = JSON.parse(data);
            console.log(connName + " From Server: " + rcvData.year);
            this.end();
        });
        this.on('end', function () {
            console.log(connName + ' Client disconnected');
        });
        this.on('error', function (err) {
            console.log('Socket Error: ', JSON.stringify(err));
        });
        this.on('timeout', function () {
            console.log('Socket Timed Out');
        });
        this.on('close', function () {
            console.log('Socket Closed');
        });
    });
    return client;
}

function writeData(socket, data) {
    var success = !socket.write(data);
    if (!success) {
        (function (socket, data) {
            socket.once('drain', function () {
                writeData(socket, data);
            });
        })(socket, data);
    }
}

// http server
var promise = require('promise');
var express = require('express');
var app = express();
var path = require('path');
var bodyParser = require('body-parser');

function onApp()
{
    app.locals.pretty = true;
    
    app.set('view engine', 'pug');
    app.set('views', path.join(__dirname, 'views'));
    
    app.use(express.static('public'));
    app.use(bodyParser.urlencoded({ extended: false }));
    
    app.get('/', function (req, res) {
        res.render('index');
        socket = getConnection("Java TCP Server");
    });

    app.listen(8080, function () {
        console.log('http server> listening on port 8080...');
    });
}

var request = function(callback, res, req) {
    
}

app.post('/result', function (req, res) {
    var jsonData = { 
        name: req.body.name,
        year: req.body.year
    };

    console.log(JSON.stringify(jsonData));
    writeData(socket, JSON.stringify(jsonData));

    socket.end();
    console.log('render called');
    res.render('result', {
        name: req.body.name,
        year: req.body.year
    });
});

onApp();
