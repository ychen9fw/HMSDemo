const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//文章：攻略和故事文章
const commentSchema = new Schema({
    id:{
        type:String,
        required:true,
        unique:true
    },
    ownerUserId:{
        type:String,
        required:true
    },
    targetUserId:{
        type:String,
        required:true
    },
    parentId:{
        type:String,
        required:true
    },
    parentType:{
        type:String,
        require:true
    },
    createdAt:{
        type:Date,
        require:true
    },
    likes:{
        type:Number,
        required:true
    },
    tag:[]
})

module.exports = mongoose.model('comment', commentSchema, 'Comment');