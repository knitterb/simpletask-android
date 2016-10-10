package nl.mpcjanssen.simpletask

import android.content.Context
import com.getpebble.android.kit.PebbleKit
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver
import com.getpebble.android.kit.util.PebbleDictionary
import nl.mpcjanssen.simpletask.task.TToken
import nl.mpcjanssen.simpletask.task.TodoList
import java.security.MessageDigest
import java.util.*

/**
 * Created by brandonknitter on 10/5/16.
 */


class PebbleReceiver(subscribedUuid: UUID?) : PebbleDataReceiver(subscribedUuid) {


    override fun receiveData(context: Context?, transactionId: Int, data: PebbleDictionary?) {
        log.debug(TAG, "receiveData...")

        val messageTypeRequest = data?.getInteger(0); // hardcoded to be field 0

        PebbleKit.sendAckToPebble(context, transactionId);

        when(messageTypeRequest) {
            MessageTypeRequestAllTasks -> requestAllTasks(context, transactionId, data)
            else -> log.debug(TAG, "Bad requestType: " + messageTypeRequest)
        }

    }

    fun requestAllTasks(context: Context?, transactionId: Int, data: PebbleDictionary?) {
        val pebbleIsConnected = PebbleKit.isWatchConnected(context)
        val pebbleAppMessageSupported = PebbleKit.areAppMessagesSupported(context)
        log.debug(TAG, "Is watch connected? " + pebbleIsConnected)
        log.debug(TAG, "Is message supported? " + pebbleAppMessageSupported)

        nextTask=0;
        sendNextTask(context);

    }

    companion object {
        val appUuid = UUID.fromString("a6c5b8ef-0b0e-4db2-8ebe-32a363699065")

        val log = Logger

        @JvmStatic var nextTask=0;
        @JvmStatic fun sendNextTask(context: Context?) {
            if (TodoList.todoItems.size <= nextTask) {
                log.debug(TAG, "No more tasks to send at "+ nextTask);
                return;
            }
            val item=TodoList.todoItems.get(nextTask);
            nextTask++;

            val dict = PebbleDictionary()

            // just get the text portion out for now
            val tokensToShow = TToken.ALL and TToken.TEXT
            // TODO(bk) need to trim this to a maximum length
            val text = item.task.showParts(tokensToShow)

            // generate hash
            val md=MessageDigest.getInstance("MD5")
            md.update(item.task.text.toByte())
            val hash=String(md.digest()).substring(21,31)

            dict.addInt32(0, MessageTypeResponseTask);
            dict.addInt32(MessageTypeResponseTaskId, item.line.toInt());
            dict.addString(MessageTypeResponseTaskHash, hash)
            dict.addString(MessageTypeResponseTaskName, text)

            log.debug(TAG, "Sending task: " + item.line + "=" + text+" with UUID: "+ appUuid);
            PebbleKit.sendDataToPebbleWithTransactionId(context, appUuid, dict, 42)
        }


        // Watch to Phone request types (field 0)
        val MessageTypeRequestAllTasks:Long=0;
        val MessageTypeRequestCompleteTask=1;

        // Phone to Watch Response Types (field 0)
        val MessageTypeResponseTask=0;

        // Fields: MessageTypeResponseTask=0
        val MessageTypeResponseTaskId=1;
        val MessageTypeResponseTaskHash=2;
        val MessageTypeResponseTaskName=3;

        private val TAG = "PebbleReceiver"
    }

}
