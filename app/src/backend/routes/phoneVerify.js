
const express = require('express');
const router = express.Router();
const phoneVerifyController = require('../controllers/phoneVerifyController')
//需要前台发来手机号码，还有目的：0表示是登录，1表示是找回密码
router.post('/verifyPhone', phoneVerifyController.sendMessage);

module.exports = router;