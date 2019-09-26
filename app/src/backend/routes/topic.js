const express = require('express');
const router = express.Router();
const topicController = require('../controllers/topicController');
const asyncMiddleware = require('../middlewares/asyncMiddleware')
//话题相关的api
router.get('/getTopic/', asyncMiddleware(topicController.getTopic));
router.post('/postAnswer/', topicController.postAnswer);
router.get('/getTopicAnswer/', topicController.getTopicAnswer);

module.exports = router;
