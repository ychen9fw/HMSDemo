const express = require('express');
const router = express.Router();
const spuController = require('../controllers/spuController')

router.get('/getSpu/:id')

module.exports = router;