const createError = require('http-errors');
const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');
const logger = require('morgan');
const passport = require('passport');
const mongoose = require("mongoose");
const bodyParser = require('body-parser');
const redis = require('redis');

require('./middlewares/passport');

const indexRouter = require('./routes/index');
const usersRouter = require('./routes/users');
const phoneVerifyRouter = require('./routes/phoneVerify');
const authRouter = require('./routes/auth');
const topicRouter = require('./routes/topic');
const virtualProductRouter = require('./routes/virtualProduct');

const dbConfig = require('./configs/db');

const app = express();

mongoose.connect(dbConfig.url, { useNewUrlParser: true }, function(err) {
  if(err) {
    console.log('connection error', err);
  } else {
    console.log('connection successful');
  }
});

const redisClient = redis.createClient({
  port      : 6379,               // replace with your port
  host      : 'SG-lunahome-24958.servers.mongodirector.com',        // replace with your hostanme or IP address
  password  : 'dNhrS3PVNwYVq2cHE49CV577ESHIIU7e',    // replace with your password
  // optional, if using SSL
  // use `fs.readFile[Sync]` or another method to bring these values in
 /* tls       : {
    key  : stringValueOfKeyFile,
    cert : stringValueOfCertFile,
    ca   : [ stringValueOfCaCertFile ]
  }*/
});
redisClient.on('connect', function(){
  console.log('Connected to Redis');
});

redisClient.on('error', function(err) {
  console.log('Redis error: ' + err);
});

const client = require('./configs/elasticsearch.js');

client.cluster.health({},function(err,resp,status) {
  console.log("-- Client Health --",resp);
});

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));
app.use(passport.initialize());
app.use(bodyParser.urlencoded({
  extended: true
}));

// parse application/json
app.use(bodyParser.json())

app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/auth', authRouter);
app.use('/phoneVerify', phoneVerifyRouter);
app.use('/topic', topicRouter);
app.use('/item', virtualProductRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports.redisClient = redisClient;
module.exports = app;
