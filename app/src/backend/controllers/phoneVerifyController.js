const Core = require('@alicloud/pop-core')
const phoneVerifyCode = require('../utils/phoneVeifyCode')
const redisService = require('../middlewares/redisService')

exports.sendMessage = function(req, res, next) {
  const phoneNumber = req.body.phonenumber
  const verifyCode = phoneVerifyCode.generateCode
  const purpose = req.body.purpose

  //首先检查用户手机是否存在吗？
  
    const client  = new Core({
        accessKeyId: 'LTAIAjSdhrPgHri2',
        accessKeySecret:'or2GSezGiC3PeP2hYEi3nMdXcXu9fB',
        endpoint:'https://dysmsapi.aliyuncs.com',
        apiVersion:'2017-05-25'
    });


    const params = {
        "RegionId" : "default",
        "TemplatePram": "SMS_173175230",
        "TemplateCode" : verifyCode,
        "SignName" : "测试用-lunahome",
        "PhoneNumbers" : phoneNumber
    };

    const requestOption = {
        method: 'POST'
    };

    client.request('SendSms', params, requestOption).then((result) => {
        console.log(JSON.stringify(result));
        if (result.code == 200) {
            redisService.savePhoneVerifyCode(phoneNumber, verifyCode, purpose).then((response) =>{
                if(response.result == true){
                    res.status(200).send({"msg":"验证码发送成功，请在两分钟内输入验证码验证"})
                }else{
                    res.status(500).send({"msg":response.msg})
                }
            })
        } else {
            res.send(result)
        }
    }, (ex) => {
        console.log(ex);
    })
}
