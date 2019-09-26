const redisService = require('../middlewares/redisService')
const jwtGenerater = require('../utils/generateJWTCode')

exports.verifyCodeForPassword = function(req, res, next) {
  const phoneNumber = req.body.phoneNumber;
  const verifyCode = req.body.verifycode;
  // VERIFY PHONE CODE FOR PASSWORD RESET DATABASE = 1
  const PURPOSE = 1;
  try{
      redisService.comparePhoneVerifyCode(phoneNumber,verifyCode, PURPOSE).then(response =>{
          if(response.result == true){
            //生成一个临时TOKEN 加上电话号码 和过期时间发送给前台
            const token = jwtGenerater.generateTempJWTCode(phoneNumber);
            res.status(200).send({
                verified: true,
                token: token,
                message: '手机号码验证成功',
            });
          }else{
            res.status(500).send({"msg":response.msg})
          }
      })
  }catch (err) {
      console.log(err)
  }
  next()
}

exports.setForgotPassword = function(req, res, next){
  //TODO: 从前台获得TOKEN 和新的密码，检测TOKEN是否在过期列表中，不在的话获取TOKEN中的手机号码，检测过期时间，然后对用户进行更新，更新结束后，将token加入到Redis的过期列表中
    inBlacklist = redisService.isUserTokenInBlackList(jwt_payload);
    if (inBlacklist == true){
      res.status(401).send({
          result: false,
          message: '验证以超时, 请重新获取验证码',
      });
    }


}
