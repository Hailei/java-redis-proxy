package com.zhh.redis.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

import com.zhh.redis.command.ArrayReply;
import com.zhh.redis.command.BulkReply;
import com.zhh.redis.command.ErrorReply;
import com.zhh.redis.command.IntegerReply;
import com.zhh.redis.command.RedisReply;
import com.zhh.redis.command.RedisReply.Type;
import com.zhh.redis.command.StatusReply;

public class RedisRequestDecoder extends ReplayingDecoder<RedisRequestDecoder.State> {

	public static final char DOLLAR_BYTE = '$';
    public static final char ASTERISK_BYTE = '*';
    public static final char COLON_BYTE=':';
    public static final char OK_BYTE='+';
    public static final char ERROR_BYTE='-';
    public static final char CR_BYTE = '\r';
    public static final char LF_BYTE = '\n';
    private RedisReply reply;
    
	protected enum State {
		READ_SKIP,//健壮性考虑，如果第一个字符不是*则skip直到遇到*
		READ_INIT, 
	    READ_ARRAY_LEN, 
	    READ_INTEGER,
	    READ_BULK,
	    READ_ERR,
	    READ_STATUS,
	    READ_TYPE,
	   READ_END
		
	}
	
	public  RedisRequestDecoder(){
		super(State.READ_SKIP);
	}
	
private static void skipChar(ChannelBuffer buffer){
		
		for(;;){
		    char ch = (char)buffer.readByte();
		    if(ch == ASTERISK_BYTE || ch == DOLLAR_BYTE  ||  ch == COLON_BYTE || ch == OK_BYTE  ||  ch == ERROR_BYTE  ){
		    	buffer.readerIndex(buffer.readerIndex() - 1);
		    	break;
		    }
		}
	}

private static int readInt(ChannelBuffer buffer){
	
	int result = Integer.parseInt(readLine(buffer));//TODO  转型安全
	return result;
}


private static String readLine(ChannelBuffer buffer){
	StringBuilder sb = new StringBuilder();
	char ch = (char)buffer.readByte();
	while(ch != CR_BYTE){//TODO 或许需要做一些 判断例如长度判断防止死循环
		sb.append(ch);
		ch = (char)buffer.readByte();
	}
	buffer.readByte();//TODO  是否需要增加对LF的判断
	
	return sb.toString();
}

@Override
protected Object decode(ChannelHandlerContext ctx, Channel channel,
		ChannelBuffer buffer, State state) throws Exception {
	switch (state) {
	case READ_SKIP:{
		try{
			skipChar(buffer);
			checkpoint(State.READ_INIT);
		}finally{
			checkpoint();
		}
	}
	case READ_INIT: {
           char ch = (char)buffer.readByte();
           if(ch == ASTERISK_BYTE){
        	  checkpoint(State.READ_ARRAY_LEN);
           }else if ( ch == DOLLAR_BYTE){
        	   reply = new BulkReply();
        	   checkpoint(State.READ_BULK);
           }else if ( ch == COLON_BYTE ){
        	   reply = new IntegerReply();
        	   checkpoint(State.READ_INTEGER);
           }else if ( ch  == OK_BYTE ){
        	   reply = new StatusReply();
        	   checkpoint(State.READ_STATUS);
           }else if( ch == ERROR_BYTE ){
        	   reply = new ErrorReply();
        	   checkpoint(State.READ_ERR);
           }
	}
	case READ_ARRAY_LEN:{
	    int  count = readInt(buffer);
	    buffer.skipBytes(2);//skip CLRF
	    reply = new ArrayReply(count);
		checkpoint(State.READ_TYPE);
	}
	case READ_TYPE:{
		char type = (char)buffer.readByte();
		if(type == COLON_BYTE){
			checkpoint(State.READ_INTEGER);
		}else if(type == DOLLAR_BYTE){
			checkpoint(State.READ_BULK);
		}
	}
	case READ_STATUS:{
		byte[] value  = readLine(buffer).getBytes();
		((StatusReply)reply).setValue(value);
		checkpoint(State.READ_END);
	}
	case READ_ERR:{
		byte[] value  = readLine(buffer).getBytes();
		((ErrorReply)reply).setValue(value);
		checkpoint(State.READ_END);
	}
	case READ_INTEGER:{
		byte[] value = readLine(buffer).getBytes();
		 if(reply.getType() == Type.INTEGER){
		    	((IntegerReply)reply).setValue(value);
		    	checkpoint(State.READ_END);
		    }else if (reply.getType() == Type.ARRAY){
		    	IntegerReply integerReply = new IntegerReply(value);
		    	ArrayReply arryReply = ((ArrayReply)reply);
		    	arryReply.addReply(integerReply);
		    	if(arryReply.complete()){
		    		checkpoint(State.READ_END);
		    	}else{
		    		checkpoint(State.READ_TYPE);
		    	}
		    }
	}
	case READ_BULK:{
			int length = readInt(buffer);
			if (length == -1) {
				buffer.skipBytes(2);
				if (reply.getType() == Type.BULK) {
					checkpoint(State.READ_END);
				} else if (reply.getType() == Type.ARRAY) {
					BulkReply bulk = new BulkReply();
					ArrayReply arryReply = ((ArrayReply) reply);
					arryReply.addReply(bulk);
					if (arryReply.complete()) {
						checkpoint(State.READ_END);
					} else {
						checkpoint(State.READ_TYPE);
					}
				}
			} else {
				byte[] value = new byte[length];
				buffer.readBytes(value);
				buffer.skipBytes(2);// skip \r\n
				if (reply.getType() == Type.BULK) {
					((BulkReply) reply).setValue(value);
					checkpoint(State.READ_END);
				} else if (reply.getType() == Type.ARRAY) {
					BulkReply bulk = new BulkReply(value);
					ArrayReply arryReply = ((ArrayReply) reply);
					arryReply.addReply(bulk);
					if (arryReply.complete()) {
						checkpoint(State.READ_END);
					} else {
						checkpoint(State.READ_TYPE);
					}
				}
			}
		
	}
	case READ_END:{
		RedisReply reply = this.reply;
		this.reply = null;
		checkpoint(State.READ_INIT);
		return reply;
	}
	default:
		throw new Error("can't  reach there!");
	}
}

}
