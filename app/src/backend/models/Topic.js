const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//话题
const topicSchema = new Schema({
    id:{
        type:String,
        required:true,
        unique:true
    },
    title:{
        type:String,
        required:true
    },
    description:{
        type:String
    },
    follower:[],
    answerNumber:{
        type:Number,
        require:true
    },
    answers:[],
    likes:{
        type:Number,
        required:true
    },
    publisher:{
        type:String,
        required:true
    },
    createdAt:{
        type:Date,
        require:true
    },

    tag:[]
})

module.exports = mongoose.model('topic', topicSchema, 'Topic');
