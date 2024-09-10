package expo.modules.notifications.notifications.model

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.messaging.RemoteMessage
import expo.modules.notifications.notifications.enums.NotificationPriority
import expo.modules.notifications.notifications.interfaces.INotificationContent
import expo.modules.notifications.notifications.presentation.builders.downloadImage
import org.json.JSONObject

/**
 * A POJO representing a remote notification content: title, message, body, etc.
 * The content originates in a RemoteMessage object.
 *
 * Instances of this class are not persisted in SharedPreferences (unlike {@link NotificationContent}. This class does
 * not implement Serializable, but Parcelable ensures we can pass instances between different parts of the application.
 */
class RemoteNotificationContent(private val remoteMessage: RemoteMessage) : INotificationContent {

    constructor(parcel: Parcel) : this(parcel.readParcelable<RemoteMessage>(RemoteMessage::class.java.classLoader)!!)

    override fun getTitle(): String? = remoteMessage.notification?.title

    override fun getText(): String? = remoteMessage.notification?.body

    override fun shouldPlayDefaultSound(): Boolean = remoteMessage.notification?.sound == null

    override fun getSoundName(): String? {
        return remoteMessage.notification?.sound
    }

    override fun shouldUseDefaultVibrationPattern(): Boolean = remoteMessage.notification?.defaultVibrateSettings == true

    override fun getVibrationPattern(): LongArray? = remoteMessage.notification?.vibrateTimings

    override fun getColor(): Number? = remoteMessage.notification?.color?.let { android.graphics.Color.parseColor(it) }

    override suspend fun getImage(context: Context): Bitmap? {
        val uri = remoteMessage.notification?.imageUrl
        return uri?.let { downloadImage(it) }
    }

    override fun containsImage(): Boolean {
        return remoteMessage.notification?.imageUrl != null
    }

    override fun getBody(): JSONObject? = try {
        JSONObject(remoteMessage.data["body"] ?: "{}")
    } catch (e: Exception) {
        null
    }

    override fun getPriority(): NotificationPriority = when (remoteMessage.priority) {
        RemoteMessage.PRIORITY_HIGH -> NotificationPriority.HIGH
        else -> NotificationPriority.DEFAULT
    }

    // NOTE the following getter functions are here because the local notification content class has them
    // and this class conforms to the same interface. They are not supported by FCM.
    override fun getSubtitle(): String? = remoteMessage.data["subtitle"]

    override fun getBadgeCount(): Number? = remoteMessage.data["badge"]?.toIntOrNull()

    override fun isAutoDismiss(): Boolean = remoteMessage.data["autoDismiss"]?.toBoolean() ?: true

    override fun getCategoryId(): String? = remoteMessage.data["categoryId"]

    override fun isSticky(): Boolean = remoteMessage.data["sticky"]?.toBoolean() ?: false

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(remoteMessage, flags)
    }

    companion object CREATOR : Parcelable.Creator<RemoteNotificationContent> {
        override fun createFromParcel(parcel: Parcel): RemoteNotificationContent {
            return RemoteNotificationContent(parcel)
        }

        override fun newArray(size: Int): Array<RemoteNotificationContent?> {
            return arrayOfNulls(size)
        }
    }
}