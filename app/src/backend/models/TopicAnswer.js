const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//话题
const topicAnswerSchema = new Schema({
    id:{
        type:String,
        required:true,
        unique:true
    },
    ownerUserId:{
        type:String,
        required:true
    },
    topicId:{
        type:String,
        required:true
    },
    createdAt:{
        type:Date,
        require:true
    },
    description: {
        type:String
    },
    pictures:[],
    likes:{
        type:Number,
        required:true
    },
    tag:[]
})

module.exports = mongoose.model('topicAnswer', topicAnswerSchema, 'TopicAnswer');