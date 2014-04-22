var fs = require('fs');
var express = require('express');
var app = express();
var gcm = require('node-gcm');
var bodyParser = require('body-parser');
var serveStatic = require('serve-static');

app.use(bodyParser());
app.use(serveStatic('.', {'index': 'index.html'}));

var ids = []; // TODO: should not keep in memory for production

app.post('/register', function (req, res) {
  // curl -X POST http://localhost:3000/register --data "regId=test"
  var regId = req.body.regId;
  if (regId) {
    ids.push(regId);
    console.log('adding id', regId);
  } else {
    console.log('no id');
  }
  res.end();
});

var apikey = fs.readFileSync('./apikey').toString().trim();
var sender = new gcm.Sender(apikey);

app.post('/send', function (req, res) {
  // curl -X POST http://localhost/send --data "data=hello world"
  var data = req.body.data;
  if (data) {
    console.log('got data, sending');

    var message = new gcm.Message();

    message.addDataWithKeyValue('data', data);

    sender.send(message, ids, 4, function (err, result) {
      console.log(err, result);

      res.end(err + ':' + result);
    });
  } else {
    console.log('no data');

    res.end('no data');
  }

  res.end();
});

app.listen(3000);
return;
