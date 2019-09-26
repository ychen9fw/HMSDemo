var express = require('express');
var router = express.Router();
var User = require( '../models/User');
var JWTsecret = require('../configs/secretKey');
var passport = require('passport')
var jwt = require('jsonwebtoken');
var phoneVerifyController = require('../controllers/phoneVerifyController')
var userController = require('../controllers/userController')

/* register user. */
router.post('/register', function(req, res, next){
    passport.authenticate('register', (err, user, info) => {
        if (err) {
            console.log("this is error" + err);
            res.send(err);
        }
        if (info != undefined) {
            console.log("this is info")
            res.send(info);
        }else {
            req.logIn(user, err => {
                const data = {
                    phonenumber : req.body.phonenumber,
                    password : req.body.password
                };
                User.findOne({
                    phonenumber: data.phonenumber
                }).then(user => {
                    user.update({
                        phonenumber: data.phonenumber,
                    }).then(() => {
                        console.log('user created in db');
                        res.status(200).send({ message: 'user created' });
                    });
                });
            });
        }
    })(req, res, next);
});

router.get('/getUserProfile', function(req, res, next){
    passport.authenticate('jwt', { session: false }, (err, user, info) => {
        if (err) {
            console.log(err);
            res.send(err.message);
        }
        if (info != undefined) {
            console.log(info.message);
            res.send(info.message);
        } else {
            console.log('user found in db from route');
            res.status(200).send({
                auth: true,
                phoneNumber: user.phoneNumber,
                username: user.nickName,
                password: user.password,
                message: 'user found in db',
            });
        }
    })(req, res, next);
});

//user setForgotPassword password, 需要一个JWT token 来防止用户// 首先要检查用户是否存在
router.get('/setForgotPassword', userController.setForgotPassword);
router.get('/verifyCodeForPassword', userController.verifyCodeForPassword);


//router.get('/logout', userController.logOut);

module.exports = router;
