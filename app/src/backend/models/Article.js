const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//文章：攻略和故事文章
const articleSchema = new Schema({
    id:{
        type:String,
        required:true,
        unique:true
    },
    title:{
        type:String,
        required:true
    },
    descriptionLink:{
        type:String
    },
    category:{
        type: String
    },
    frontPicture:{
        type:String,
        required: true
    },
    publisher:{
        type:String,
        required:true
    },
    likes:{
        type:Number,
        required:true
    },
    tag:[],
    spuGroup:[]
})

module.exports = mongoose.model('article', articleSchema, 'Article');
