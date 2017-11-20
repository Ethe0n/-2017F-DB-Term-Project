var net = require('net');
const tcp_port = 4321;
const http_port = 8080;

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
    res.render('index', {
        potatoes: [
            {
                id : 1,
                name : 'french fries',
                year : 2015
            },
            {
                id : 2,
                name : 'smashed potato',
                year : 2016
            },
            {
                id : 3,
                name : 'freaky potato',
                year : 2017
            }
        ]
    });
});

app.post('/api/region', function(req, res) {
    console.log('render /api/region ');
	res.render('index', {
        potatoes: [
            {
                id : 3,
                name : 'potato is fried',
                year : 2017
            },
            {
                id : 2,
                name : 'potato is smashed',
                year : 2016
            },
            {
                id : 1,
                name : 'potato is freaky',
                year : 2015
            }
        ]
    });
});

app.listen(http_port, function () {
    console.log('http server> listening on port 8080...');
});

app.post('/result', function (req, res) {
    async.waterfall([
        function(callback) {
            var jsonData = {
                name: req.body.name,
                year: req.body.year
            };
            socket = net.connect({ port: tcp_port, host: '127.0.0.1' });
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