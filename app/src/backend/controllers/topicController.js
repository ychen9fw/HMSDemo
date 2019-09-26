const Topic = require( '../models/Topic');
const TopicAnswer = require('../models/TopicAnswer')

exports.getTopicAnswer = async function(req, res, next) {
    let id = req.query.id;
    let topic =  await service.getTopic(id);
    res.status(200).send(topic);

    TopicAnswer.findOne({
        id:id
    }).then( topicAnswer => {
        if (topicAnswer != null) {
            //user exists, send token back
            res.status(200).send({
                "topicAnswer": topicAnswer
            });
        }else {
            res.status(500).send({
                "msg": "无法找到，请稍后再试"
            })
        }
    })
}

exports.postAnswer = function(req, res, next){

    TopicAnswer.create(req.body, function(newAnswer, err){
        if(err){
            res.status(401).send({
            })
        }else{
            res.status(200).send({
                "newAnswer": newAnswer
            });
        }
    })
}

exports.getTopic = function(req, res, next){
    let id = req.query.id;
    Topic.findOne({
        id:id
    }).then( topic => {
        if (topic != null) {
            //user exists, send token back
            res.status(200).send({
                "topic": topic
            });
        }else {
            res.status(500).send({
                "msg": "无法找到，请稍后再试"
            })
        }
    })
}
