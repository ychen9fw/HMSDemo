const mongoose = require('mongoose');
const Schema = mongoose.Schema;
//用户
const UserSchema = new Schema({
    phoneNumber:{
        type:String,
        unique:true
    },

    nikeName:{
        type:String,
        unique:true
    },

    password: {
        type: String
    },

    weChatOpenId:{
        type:String,
        unique:true
    },

    weiboOpenId:{
        type:String,
        unique:true
    },

    QQOpenId:{
        type:String,
        unique:true
    }
});

/* UserSchema.pre('save', function (next) {
  if(this)
    const user = this;
    user.encryptPassword(this.password, function(err){
        if(err){
            return err
        }
    });
    next();
}); */


UserSchema.methods.comparePassword = async function(password){
    const user = this;
    //Hashes the password sent by the user for login and checks if the hashed password stored in the
    //database matches the one sent. Returns true if it does else false.
    const compare = await bcrypt.compare(password, user.password);
    return compare;
}

/*UserSchema.methods.encryptPassword = function(newpass,cb){
    bcrypt.genSalt(10, function (err, salt) {
        if (err) {
            return cb(err);
        }
        bcrypt.hash(newpass, salt, function (err, hash) {
            if (err) {
                return cb(err);
            }
            cb(null, hash);
        });
    });
};*/

module.exports = mongoose.model('user', UserSchema, 'User');
