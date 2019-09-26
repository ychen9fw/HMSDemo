const JWTsecret = require('../configs/secretKey');
const jwt = require('jsonwebtoken');

exports.generateJWTCode = function(id, loginWay){
    let iat = new Date().getTime();
    const token = jwt.sign({ id: id, "iat": iat, "loginMethod": loginWay}, JWTsecret.JWTsecret, {'expiresIn': '5d' });
    return token
}

exports.generateTempJWTCode = function(id){
    let iat = new Date().getTime();
    const token = jwt.sign({ id: id, "iat": iat}, JWTsecret.JWTsecret, {'expiresIn': '5m' });
    return token
}
