const User = require( '../models/User');
const passport = require('passport')
const redisService = require('../middlewares/redisService')
const thirdPartyLoginService = require('../middlewares/thirdPartyLoginService')
const loginMethod = require("../configs/loginMethod")
const JWTTokenGenerator = require('../utils/generateJWTCode')


exports.phonePasswordLogin = function(req, res, next) {
    passport.authenticate('loginByPhonePass', (err, user, info) => {
        if (err) {
            console.log(err);
            res.send(err.message);
        }
        if (info != undefined) {
            console.log(info.message);
            res.send(info.message);
        } else {
            req.logIn(user, err => {
                User.findOne({
                    phonenumber: user.phonenumber
                }).then(user => {
                    if (user != null) {
                        //user exists, send token back
                        let token = JWTTokenGenerator.generateJWTCode(user.phonenumber, loginMethod.PHONE_LOGIN );
                        res.status(200).send({
                            auth: true,
                            token: token,
                            message: 'user found & logged in',
                        });
                    }
                });
            });
        }
    }) (req, res, next)
}

exports.phoneCodeLogin = function(req, res, next) {
    const phoneNumber = req.body.phoneNumber;
    const verifyCode = req.body.verifycode;
    // LOGIN PHONE CODE DATABASE = 0
    const PURPOSE = 0;
    try{
        redisService.comparePhoneVerifyCode(phoneNumber,verifyCode, PURPOSE).then(response =>{
            if(response.result == true){
                User.findOne({
                    phonenumber: phoneNumber
                }).then(user => {
                    if (user == null){
                       // create new user with phone.
                       User.create(user, function(newUser, err){
                           if(err){
                               res.status(401).send({
                               })
                           }else{
                             let token = JWTTokenGenerator.generateJWTCode(user.phonenumber, loginMethod.PHONE_LOGIN );
                             res.status(200).send({
                                 auth: true,
                                 token: token,
                                 message: '用户成功注册并登录'
                             });
                           }
                       })
                    } else {
                        // user exist, send token back
                        let token = JWTTokenGenerator.generateJWTCode(user.phonenumber, loginMethod.PHONE_LOGIN );
                        res.status(200).send({
                            auth: true,
                            token: token,
                            message: '用户登录成功',
                        });
                    }

                });
            }else{
                res.status(500).send({"msg":"验证码无效，请重新获取验证码"})
            }
        })
    }catch (err) {
        console.log(err)
    }
    next()
}

exports.wechatLogin = function(req, res, next){
    code = req.body.code;
    thirdPartyLoginService.getWechatToken(code).then(response => {
        if (response.result == true){
            User.findOne({
                wechatID: reponse.openID
            }).then(user => {
                if(user == null){
                    thirdPartyLoginService.getWechatUserData(response.accessToken, response.openID).then(weChatUser => {
                        if(weChatUser.result == true){
                            User.createWechatUser(weChatUser, function(newUser, err){
                                if(err){
                                    res.status(401).send({
                                    })
                                }else{
                                    let token = JWTTokenGenerator.generateJWTCode(newUser.wechatID, loginMethod.WECHAT_LOGIN );
                                    res.status(200).send({
                                        auth: true,
                                        token: token,
                                        message: '用户成功注册并登录',
                                    });
                                }
                            })

                        }
                    })

                }else{
                    //user already exist, just return success and return JWT with openID only, set expires as 5 days
                    let token = JWTTokenGenerator.generateJWTCode(user.wechatID, loginMethod.WECHAT_LOGIN );
                    res.status(200).send({
                        auth: true,
                        token: token,
                        message: '用户成功登录',
                    });
                }
            })
        }
    })

}


exports.QQLogin = function(req, res, next){
    QQopenId = req.body.openId
    accessToken = req.body.accessToken
    User.findOne({
        QQID: QQopenId
    }).then(user => {
        if(user == null){
            thirdPartyLoginService.getQQUserData(QQopenId, accessToken).then(QQUser => {
                if(QQUser.result == true){
                    User.createQQUser(QQUser, function(newUser, err){
                        if(err){
                            res.status(401).send({
                            })
                        }else{
                            let token = JWTTokenGenerator.generateJWTCode(newUser.QQopenId, loginMethod.QQ_LOGIN);
                            res.status(200).send({
                                auth: true,
                                token: token,
                                message: '用户成功注册并登录',
                            });
                        }
                    })
                }
            })
        }else{
            //user already exist, just return success and return JWT with openID only, set expires as 5 days
            let token = JWTTokenGenerator.generateJWTCode(user.wechatID, loginMethod.WECHAT_LOGIN );
            res.status(200).send({
                auth: true,
                token: token,
                message: '用户成功登录'
            });
        }
    })
}

exports.weiboLogin = function(req, res, next){
//receive accesstoken from Client, use accesstoken to get userID, if ID exists, return JWT, if ID doesn't exist, create new user, return JWT
    accessToken = req.body.accessToken
    thirdPartyLoginService.getWeiboUserID(accessToken).then(response => {
        if(response.result == true) {
            User.findOne({
                weiboID: response.openId
            }).then(user => {
                if(user == null){
                    thirdPartyLoginService.getQQUserData(response.openId, accessToken).then(weiboUser => {
                        if(weiboUser.result == true){
                            User.create(weiboUser, function(newUser, err){
                                if(err){
                                    res.status(401).send({
                                    })
                                }else{
                                    let token = JWTTokenGenerator.generateJWTCode(newUser.weiboID, loginMethod.WEIBO_LOGIN);
                                    res.status(200).send({
                                        auth: true,
                                        token: token,
                                        message: '用户成功注册并登录'
                                    });
                                }
                            })
                        }
                    })
                }else{
                    //user already exist, just return success and return JWT with openID only, set expires as 5 days
                    let token = JWTTokenGenerator.generateJWTCode(user.weiboID, loginMethod.WEIBO_LOGIN );
                    res.status(200).send({
                        auth: true,
                        token: token,
                        message: '用户成功登录',
                    });
                }
            })
        }
    })
}
