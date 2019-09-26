const express = require('express');
const router = express.Router();
const recommendationController = require('../controllers/recommendationController')

//推荐相关API
router.get('/getAll', recommendationController.getAllitem());
router.get('/getInspirations', recommendationController.getInspirations());
router.get('/getHouseExamples', recommendationController.getHouseExamples());
router.get('/getSpus', recommendationController.getSpus());

module.exports = router;
