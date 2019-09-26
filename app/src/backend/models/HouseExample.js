const mongoose = require('mongoose');
const Schema = mongoose.Schema;

//全屋
const houseExampleSchema = new Schema({
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
        type:String,
        require:true
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
   // comments:[]
})

module.exports = mongoose.model('houseExample', houseExampleSchema, 'HouseExample');
