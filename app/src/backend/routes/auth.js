var express = require('express');
var router = express.Router();
var loginController = require('../controllers/loginController')


router.post('/loginByPassword', loginController.phonePasswordLogin);
router.post('/loginByVerifyCode', loginController.phoneCodeLogin);
router.post('/loginByWechat', loginController.wechatLogin);
router.post('/loginByQQ', loginController.QQLogin);
router.post('/loginByWeibo', loginController.weiboLogin);

module.exports = router;
