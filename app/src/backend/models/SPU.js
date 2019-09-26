//物品
const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const spuSchema = new Schema({
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
    category:{
        type: String
    },
    frontPicture:{
        type:String,
        required: true
    },
    spus:[], //the subDoc is different based on the category
    publisher:{
        type:String,
        required:true
    },
    likes:{
        type:Number,
        required:true
    },
    tag:[]
})

module.exports = mongoose.model('SPU', spuSchema, 'SPU');