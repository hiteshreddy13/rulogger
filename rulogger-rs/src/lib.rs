use crossbeam_channel::{unbounded, Sender};
use jni::objects::{JClass, JString, JByteBuffer};
use jni::JNIEnv;
use std::fs::OpenOptions;
use std::io::{BufWriter, Write};
use std::sync::OnceLock;
use std::thread;
use std::time::{SystemTime, UNIX_EPOCH};
use std::slice;

static LOG_SENDER: OnceLock<Sender<LogEntry>> = OnceLock::new();

enum LogEntry {
    Message(String),
    Shutdown,
}

#[no_mangle]
pub extern "system" fn Java_com_rulogger_RuLogger_init<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    file_path: JString<'local>,
) {
    if file_path.is_null() {
        return;
    }
    let file_path_str = env.get_string(&file_path);
    let file_path: String = match file_path_str {
        Ok(s) => s.into(),
        Err(_) => String::from("rulogger.log"),
    };

    let (tx, rx) = unbounded::<LogEntry>();

    if LOG_SENDER.set(tx).is_err() {
        return;
    }

    thread::spawn(move || {
        let file = OpenOptions::new()
            .create(true)
            .append(true)
            .open(file_path)
            .expect("Failed to open log file");
        let mut writer = BufWriter::with_capacity(1024 * 1024, file);

        for entry in rx {
            match entry {
                LogEntry::Message(msg) => {
                    let _ = writeln!(writer, "{}", msg);
                }
                LogEntry::Shutdown => {
                    let _ = writer.flush();
                    break;
                }
            }
        }
    });
}

#[no_mangle]
pub extern "system" fn Java_com_rulogger_RuLogger_close<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    if let Some(sender) = LOG_SENDER.get() {
        let _ = sender.send(LogEntry::Shutdown);
    }
}

#[no_mangle]
pub extern "system" fn Java_com_rulogger_RuLogger_info__Ljava_lang_String_2<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    message: JString<'local>,
) {
    if message.is_null() {
        log_message("INFO", "null");
        return;
    }
    let message_str = env.get_string(&message);
    let message: String = match message_str {
        Ok(s) => s.into(),
        Err(_) => String::from(""),
    };
    log_message("INFO", &message);
}

// Zero-copy direct buffer logging with level
#[no_mangle]
pub extern "system" fn Java_com_rulogger_RuLogger_logDirect<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    level: jni::sys::jint,
    buffer: JByteBuffer<'local>,
    length: jni::sys::jint,
) {
    if buffer.is_null() || length <= 0 {
        return;
    }

    let level_str = match level {
        1 => "INFO",
        2 => "WARN",
        3 => "ERROR",
        _ => "DEBUG",
    };

    if let Ok(address) = env.get_direct_buffer_address(&buffer) {
        let bytes = unsafe { slice::from_raw_parts(address, length as usize) };
        if let Ok(message) = std::str::from_utf8(bytes) {
            log_message(level_str, message);
        } else {
            log_message(level_str, "<invalid utf-8 buffer>");
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_rulogger_RuLogger_warn<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    message: JString<'local>,
) {
    if message.is_null() {
        log_message("WARN", "null");
        return;
    }
    let message_str = env.get_string(&message);
    let message: String = match message_str {
        Ok(s) => s.into(),
        Err(_) => String::from(""),
    };
    log_message("WARN", &message);
}

#[no_mangle]
pub extern "system" fn Java_com_rulogger_RuLogger_error<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    message: JString<'local>,
) {
    if message.is_null() {
        log_message("ERROR", "null");
        return;
    }
    let message_str = env.get_string(&message);
    let message: String = match message_str {
        Ok(s) => s.into(),
        Err(_) => String::from(""),
    };
    log_message("ERROR", &message);
}

#[no_mangle]
pub extern "system" fn Java_com_rulogger_RuLogger_info<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    message: JString<'local>,
) {
    if message.is_null() {
        log_message("INFO", "null");
        return;
    }
    let message_str = env.get_string(&message);
    let message: String = match message_str {
        Ok(s) => s.into(),
        Err(_) => String::from(""),
    };
    log_message("INFO", &message);
}

fn log_message(level: &str, message: &str) {
    let now = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis();

    let formatted_message = format!("[{}] [{}] {}", now, level, message);

    if let Some(sender) = LOG_SENDER.get() {
        let _ = sender.send(LogEntry::Message(formatted_message));
    } else {
        println!("{}", formatted_message);
    }
}
