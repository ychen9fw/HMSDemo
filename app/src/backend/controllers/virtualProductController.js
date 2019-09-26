const HouseExample = require( '../models/HouseExample');
const Inspiration = require( '../models/Inspiration');
const Article = require( '../models/Article');
const SpuGroup = require( '../models/SpuGroups');

exports.getHouseExample = function(req, res, next) {
    let id = req.query.id;
    HouseExample.findOne({
        id:id
    }).then( houseExample => {
        if (houseExample != null) {
            //user exists, send token back
            res.status(200).send({
                "houseExample": houseExample
            });
        }else {
            res.status(500).send({
                "msg": "无法找到，请稍后再试"
            })
        }
    })
}

exports.getInspiration = function(req, res, next) {
    let id = req.query.id;
    Inspiration.findOne({
        id:id
    }).then( inspiration => {
        if (inspiration != null) {
            //user exists, send token back
            res.status(200).send({
                "inspiration": inspiration
            });
        }else {
            res.status(500).send({
                "msg": "无法找到，请稍后再试"
            })
        }
    })
}

exports.getArticle = function(req, res, next) {
    let id = req.query.id;
    Article.findOne({
        id:id
    }).then( article => {
        if (article != null) {
            //user exists, send token back
            res.status(200).send({
                "article": article
            });
        }else {
            res.status(500).send({
                "msg": "无法找到，请稍后再试"
            })
        }
    })
}

exports.getSpuGroup = function(req, res, next){
    let id = req.query.id;
    SpuGroup.findOne({
        id:id
    }).then( spuGroup => {
        if (spuGroup != null) {
            //user exists, send token back
            res.status(200).send({
                "spuGroup": spuGroup
            });
        }else {
            res.status(500).send({
                "msg": "无法找到，请稍后再试"
            })
        }
    })
}
