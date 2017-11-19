var net = require('net');

function getConnection(connName) {
    var client = net.connect({ port: 4321, host: '127.0.0.1' }, function () {
        console.log(connName + ' Connected: ');
        console.log('   local = %s:%s', this.localAddress, this.localPort);
        console.log('   remote = %s:%s', this.remoteAddress, this.remotePort);
        this.setEncoding('utf8');

        // this.on('data', function (data) {
        //     rcvData = JSON.parse(data);
        //     console.log("From Server: " + rcvData.year);
        //     this.end();
        // });

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

function writeData(socket, data, callback) {
    socket.on('data', function (data) {
        callback(data);
    });

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
var async = require('async');

app.locals.pretty = true;

app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));

app.use(express.static('public'));
app.use(bodyParser.urlencoded({ extended: false }));

app.get('/', function (req, res) {
    res.render('index');
});

app.listen(8080, function () {
    console.log('http server> listening on port 8080...');
});

app.post('/result', function (req, res) {
    async.waterfall([
        function(callback) {
            var jsonData = {
                name: req.body.name,
                year: req.body.year
            };
            socket = net.connect({ port: 4321, host: '127.0.0.1' });
            console.log('socket connected');
            callback(null, jsonData, socket);
        },
        function(data, socket, callback) {
            socket.write(JSON.stringify(data) + '\n');
            console.log('write data to server');
            var rcvData;

            socket.on('data', function(data) {
                rcvData = JSON.parse(data);
                console.log("From Server: " + rcvData.year);
                socket.end();
                callback(null, rcvData);
            });
        },
        function (data, callback) {
            if (data === undefined) {
                callback('undefined info', 'error');
            }
            else {
                res.render('result', {
                    name: data.name,
                    year: data.year
                });
                callback(null, 'render successful');
            }
        }
    ], function(err, result) {
        if (err === null) {
            console.log('run successful : ' + result);
        }
        else {
            console.log('error occurred : ' + error);
        }
    });
});