const passport = require('passport')
const JWTsecret = require('../configs/secretKey')
const localStrategy = require('passport-local').Strategy
const User = require('../models/User')
const JWTstrategy = require('passport-jwt').Strategy
const ExtractJWT = require('passport-jwt').ExtractJwt
const BCRYPT_SALT_ROUNDS = 12
const bcrypt = require('bcrypt')
const redisService = require('../middlewares/redisService')
const loginMethod = require('../configs/loginMethod')
passport.use(
    'register',
    new localStrategy(
        {
            usernameField: 'phonenumber',
            passwordField: 'password',
            session: false,
        },
        (phonenumber, password, done) => {
            try {
                console.log("find user")
                User.findOne({
                    phonenumber: phonenumber
                }).then(user => {
                    console.log(user)
                    if (user != null) {
                        console.log('username already taken');
                        return done(null, false, { message: 'username already taken' });
                    } else {
                        bcrypt.hash(password, BCRYPT_SALT_ROUNDS).then(hashedPassword => {
                            User.create({ phonenumber, password: hashedPassword }).then(user => {
                                console.log('user created');
                                // note the return needed with passport local - remove this return for passport JWT to work
                                return done(null, user);
                            });
                        });
                    }
                });
            } catch (err) {
                done(err);
            }
        },
    ),
);

passport.use(
    'loginByPhonePass',
    new localStrategy(
        {
            usernameField: 'phonenumber',
            passwordField: 'password',
            session: false
        },
        (phonenumber, password, done) => {
            try {
                User.findOne({
                    phonenumber: phonenumber
                }).then(function (user) {
                    console.log("enter")
                    if (user === null) {
                        return done(null, false, {message: '用户名不存在，请先注册用户'});
                    } else {
                        user.comparePassword(password).then(response => {
                            if (response !== true) {
                                console.log('passwords do not match');
                                return done(null, false, {message: '密码错误，请重新输入正确密码'});
                            }
                            console.log('user found & authenticated');
                            // note the return needed with passport local - remove this return for passport JWT
                            return done(null, user);
                        });
                    }
                });
            } catch (err) {
                done(err);
            }
        },
    ),
);


const opts = {
    jwtFromRequest: ExtractJWT.fromAuthHeaderWithScheme('JWT'),
    secretOrKey: JWTsecret.JWTsecret,
};

passport.use(
    'jwt',
    new JWTstrategy(opts, (jwt_payload, done) => {
        try {
            //验证用户是否之前已登出
            inBlacklist = redisService.isUserTokenInBlackList(jwt_payload);
            if (inBlacklist == true){
                done(null, false, {"msg":"token已无效，请重新登录"})
            }
            let loginWay = jwt_payload["loginMethod"]
            //验证用户登录方式，根据不同的ID查找用户
            if (loginWay == loginMethod.WECHAT_LOGIN){
                User.findOne({
                    wechatID: jwt_payload.id,
                }).then(user => {
                    if (user) {
                        console.log('user found in db');
                        // note the return removed with passport JWT - add this return for passport local
                        done(null, user, {"msg":"验证成功"});
                    } else {
                        console.log('user not found in db');
                        done(null, false, {"msg":"用户不存在，请登录"});
                    }
                });
            }
            if (loginWay == loginMethod.PHONE_LOGIN){
                User.findOne({
                    phoneNumber: jwt_payload.id,
                }).then(user => {
                    if (user) {
                        console.log('user found in db');
                        // note the return removed with passport JWT - add this return for passport local
                        done(null, user, {"msg":"验证成功"});
                    } else {
                        console.log('user not found in db');
                        done(null, false, {"msg":"用户不存在，请登录"});
                    }
                });
            }

            if (loginWay == loginMethod.QQ_LOGIN){
                User.findOne({
                    QQID: jwt_payload.id,
                }).then(user => {
                    if (user) {
                        console.log('user found in db');
                        // note the return removed with passport JWT - add this return for passport local
                        done(null, user, {"msg":"验证成功"});
                    } else {
                        console.log('user not found in db');
                        done(null, false, {"msg":"用户不存在，请登录"});
                    }
                });
            }

            if (loginWay == loginMethod.WEIBO_LOGIN){
                User.findOne({
                    weiboID: jwt_payload.id,
                }).then(user => {
                    if (user) {
                        console.log('user found in db');
                        // note the return removed with passport JWT - add this return for passport local
                        done(null, user, {"msg":"验证成功"});
                    } else {
                        console.log('user not found in db');
                        done(null, false, {"msg":"用户不存在，请登录"});
                    }
                });
            }

        } catch (err) {
            done(err);
        }
    }),
);


passport.use(
    'jwtTemp',
    new JWTstrategy(opts, (jwt_payload, done) => {
        try {
            inBlacklist = redisService.isUserTokenInBlackList(jwt_payload);
            if (inBlacklist == true){
                done(null, false, {"msg":"请求已超时，请重新设置密码"})
            }
            phoneNumber = jwt_payload.id
            done(null, phoneNumber)
        } catch (err) {
            done(err);
        }
    }),
);
