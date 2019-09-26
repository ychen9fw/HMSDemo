const express = require('express');
const router = express.Router();
const virtualProductController= require('../controllers/virtualProductController')

router.get('/getSpuGroup', virtualProductController.getSpuGroup);
router.get('/getArticle', virtualProductController.getArticle);
router.get('/getInspiration', virtualProductController.getInspiration);
router.get('/getHouseExample', virtualProductController.getHouseExample);

module.exports = router;

