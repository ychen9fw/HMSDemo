const redisClient = require('.././app').redisClient
const MAXIMUMCOUNT = 3;
/*
    Store Phone verification code in redis
 */
exports.saveUserTokenBlackList = function(token){
    redisClient.LPUSH('token', token, (err, reply) =>{
        if(reply){
            return true;
        }
        else {
            return false;
        }
    });

}

exports.isUserTokenInBlackList = function(token){
    const result = redisClient.lrange('token',0,99999999)
    if(result.indexOf(token) > -1){
        return true;
    }
}

exports.savePhoneVerifyCode = function(phoneNum, verifyCode, purpose) {
    try {
        redisClient.select(purpose, function (err, res) {
            return redisClient.hmget(phoneNum, (err, verificationCode) => {
                if (verificationCode) {
                    // user wants to receive the verification code again, check the valid time, if it is within 2 minutes, send please wait msg
                    let createTime = verificationCode["createTime"]
                    const expiration = 120; //2 minutes of valid time
                    let offsetSeconds = (Date.now() - createTime) / 1000
                    if (offsetSeconds < expiration) {
                        return {"result": false, "msg": "验证失败，请等待 (expiration - offsetSeconds) 在获取"}
                    }
                } else {
                    let createTime = Date.now();
                    let usedCount = 0;
                    let expiration = 120;
                    redisClient.hmset(phoneNum, "verifyCode", verifyCode, "usedCount", usedCount, "createTime", createTime, (err, reply) => {
                        if (err) {
                            return {"result": false, "msg": "验证失败"}
                        } else {
                            redisClient.expires(phoneNum, expiration, (err, reply) => {
                                if (err) {
                                    return {"result": false, "msg":err.msg}
                                } else {
                                    return {"result": true, "msg":"验证成功", "verifyCode": verifyCode}
                                }
                            })

                        }
                    })
                }

            })
        });
    }catch (err) {
        console.log(err)
    }
}

// TODO: pass a param: LOGIN 0 or SETPASSWORD = 1, to seperate the redis phone code database for different verify purpose
exports.comparePhoneVerifyCode = function(phoneNum, verifyCode, purpose) {
    try {
        redisClient.select(purpose, function (err, res) {
            return redisClient.hmget(phoneNum, (err, verificationCode) => {
                if (verificationCode) {
                    // the phonenumber and verificaiton code exists in redis, compare the verification code
                    let verifyCount = verificationCode["usedCount"]
                    let storedCode = verificationCode["verifyCode"]
                    if (verifyCount < MAXIMUMCOUNT) {
                        if (storedCode == verifyCode) {
                          //after find the info, delete the code,to prevent user use phone verification code again.
                            redisClient.delete(phoneNum, (err, reply) => {
                                return {"result": true}
                            });

                        }else {
                            verificationCode["verifyCode"] = verificationCode["verifyCode"] + 1;
                            return {"result": false, "msg": "验证码错误，请重新输入"}
                        }
                    }else{
                        redisClient.delete(phoneNum, (err, reply) => {
                            return {"result": false, "msg": "您已经输入错误的验证码超过三次，请重新获取验证码"}
                        });
                    }

                } else {
                    return {"result": false, "msg": "验证码已过期，请重新获取验证码"}
                }

            })
        });
    }catch  (err) {
        console.log(err)
    }

}
