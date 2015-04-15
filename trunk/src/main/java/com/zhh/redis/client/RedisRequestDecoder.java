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
		READ_INIT, 
	    READ_REPLY,
	    READ_END
	}
	
	public  RedisRequestDecoder(){
		super(State.READ_INIT);
	}
	
//private static void skipChar(ChannelBuffer buffer){
//		
//		for(;;){
//		    char ch = (char)buffer.readByte();
//		    if(ch == ASTERISK_BYTE || ch == DOLLAR_BYTE  ||  ch == COLON_BYTE || ch == OK_BYTE  ||  ch == ERROR_BYTE  ){
//		    	buffer.readerIndex(buffer.readerIndex() - 1);
//		    	break;
//		    }
//		}
//	}

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
	buffer.skipBytes(1);
	
	return sb.toString();
}

@Override
protected Object decode(ChannelHandlerContext ctx, Channel channel,
		ChannelBuffer buffer, State state) throws Exception {
	switch (state) {
	case READ_INIT: {
           char ch = (char)buffer.readByte();
           if(ch == ASTERISK_BYTE){
           }else if ( ch == DOLLAR_BYTE){
        	   reply = new BulkReply();
           }else if ( ch == COLON_BYTE ){
        	   reply = new IntegerReply();
           }else if ( ch  == OK_BYTE ){
        	   reply = new StatusReply();
           }else if( ch == ERROR_BYTE ){
        	   reply = new ErrorReply();
           }
           checkpoint(State.READ_REPLY);
	}
	case READ_REPLY:{
		Type type = reply.getType();
		if(type == Type.INTEGER){
			byte[] value = readLine(buffer).getBytes();
			((IntegerReply)reply).setValue(value);
		}else if(type == Type.STATUS){
			byte[] value  = readLine(buffer).getBytes();
			((StatusReply)reply).setValue(value);
		}else if(type == Type.ERROR){
			byte[] value  = readLine(buffer).getBytes();
			((ErrorReply)reply).setValue(value);
		}else if(type == Type.BULK){
			this.reply = readBulkReply(buffer);
		}else if(type == Type.ARRAY){
			this.reply = readArrayReply(buffer);
		}
		RedisReply reply = this.reply;
		this.reply = null;
		checkpoint(State.READ_INIT);
		return reply;
			
	}
	default:
		throw new Error("can't  reach there!");
	}
}

private BulkReply readBulkReply(ChannelBuffer buffer){
	
	BulkReply bulkReply = new BulkReply();
	int length = readInt(buffer);
	bulkReply.setLength(length);
	if (length == -1) {//read null
		
	} else if(length == 0){// read ""
		buffer.skipBytes(2);
	} else {
		byte[] value = new byte[length];
		buffer.readBytes(value);
		bulkReply.setValue(value);
		buffer.skipBytes(2);// skip \r\n
		
	} 
	
	return bulkReply;
}

private ArrayReply readArrayReply(ChannelBuffer buffer){
	int  count = readInt(buffer);
	ArrayReply  arrayReply = new ArrayReply(count);
	for(int i = 0; i < count;i++){
		char type = (char)buffer.readByte();
		if(type == COLON_BYTE){
			IntegerReply intReply =  new IntegerReply();
			intReply.setValue(readLine(buffer).getBytes());
			arrayReply.addReply(intReply);
		}else if(type == DOLLAR_BYTE){
            arrayReply.addReply(readBulkReply(buffer));			
		}
	}
	
	return arrayReply;
}

}
