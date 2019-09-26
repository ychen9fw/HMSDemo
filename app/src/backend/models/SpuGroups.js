const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//全屋集，灵感集，好物集
const spuGroupsSchema = new Schema({
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
    spus:[], //only store id of the sub spu
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

module.exports = mongoose.model('spuGroup', spuGroupsSchema, 'SpuGroup');
