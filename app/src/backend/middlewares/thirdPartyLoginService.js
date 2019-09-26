const request  = require('request');
const redisService = require("./redisService")

exports.getWechatToken = function(code) {
    let firstVerifyUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    request({
        url:firstVerifyUrl,
        method:"GET",
        json:true,
    }, function(error, response, body){
        if(response.status == 200){
            accessToken = response.get("access_token")
            openID = response.get("openid")
            refreshToken =  response.get("refresh_token");
            expires_in = response.get("expires_in");
            return {"result":true, "accessToken": accessToken, "openID":  openID, "refreshToken":refreshToken, "expires_in":expires_in}
        }
    })
}

exports.getWechatUserData = function(openID, AccessToken, freshToken) {
    let userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + AccessToken + "&openid=" + openID;
    request({
        url:userInfoUrl,
        method:"GET",
        json:true,
    }, function(error, response, body){
        if(response.status == 200){
            user = response.user;
            return {"result":true, "accessToken": accessToken, "openID":  openID, "refreshToken":refreshToken, "expires_in":expires_in}
        }
    })
}

exports.getQQUserData = function(openID, accessToken) {
    let userInfoUrl = "https://graph.qq.com/user/get_user_info?access_token="+accessToken+"&oauth_consumer_key=appkey&openid="+openID;
    request({
        url:userInfoUrl,
        method:"GET",
        json:true,
    }, function(error, response, body){
        if(response.ret == 0){
            user = response.user;
            return {"result":true, "accessToken": accessToken, "openID":  openID, "refreshToken":refreshToken, "expires_in":expires_in}
        }
    })

}

exports.getWeiboUserData = function(accessToken) {
  let verifyTokenUrl = "https://api.weibo.com/oauth2/get_token_info";
  request({
      url:verifyTokenUrl,
      method:"POST",
      json:true,
  }, function(error, response, body){
      if(response.status == 200){
          userID = response.uid;
          /*return data:
          {
          "uid": 1073880650,
          "appkey": 1352222456,
          "scope": null,
          "create_at": 1352267591,
          "expire_in": 157679471
        } */
        let userInfoUrl = "https://api.weibo.com/2/users/show.json";
        request({
            url:userInfoUrl,
            method:"GET",
            json:true,
        }, function(error, response, body){
            if(response.status == 200){
              user = response.user;
              return {"result":true, "userInfo" : response}
            } else {
              return {"result":false, "msg": "Invalid Token"}
            }
        })
      }else{
        return {"result":false, "msg": "Invalid Token"}
      }
  })
}
