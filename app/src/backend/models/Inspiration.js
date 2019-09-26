const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//灵感
const inspirationSchema = new Schema({
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
    price:{
        type:String
    },
    category:{
        type: String
    },
    spaceType:{
        type:String
    },
    area:{
        type:String
    },
    frontPicture:{
        type:String,
        required: true
    },
    pictures:[{
        pictureUrl:{
            type:String
        },
        designPoint:[]
    }],
    publisher:{
        type:String,
        required:true
    },
    spus:[],
    likes:{
        type:Number,
        required:true
    },
    tag:[],
    spuGroup:[]
})

module.exports = mongoose.model('inspiration', inspirationSchema, 'Inspiration');
