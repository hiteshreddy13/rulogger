use jni::objects::{JClass, JString};
use jni::JNIEnv;
use std::fs::OpenOptions;
use std::io::{BufWriter, Write};
use std::sync::mpsc::{self, Sender};
use std::sync::{Mutex, OnceLock};
use std::thread;
use std::time::{SystemTime, UNIX_EPOCH};

static LOG_SENDER: OnceLock<Mutex<Sender<LogEntry>>> = OnceLock::new();

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

    let (tx, rx) = mpsc::channel::<LogEntry>();
    LOG_SENDER.get_or_init(|| Mutex::new(tx));

    thread::spawn(move || {
        let file = OpenOptions::new()
            .create(true)
            .append(true)
            .open(file_path)
            .expect("Failed to open log file");
        let mut writer = BufWriter::with_capacity(1024 * 1024, file); // 1MB buffer

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
    if let Some(sender_mutex) = LOG_SENDER.get() {
        if let Ok(sender) = sender_mutex.lock() {
            let _ = sender.send(LogEntry::Shutdown);
        }
    }
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

fn log_message(level: &str, message: &str) {
    let now = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis();

    let formatted_message = format!("[{}] [{}] {}", now, level, message);

    if let Some(sender_mutex) = LOG_SENDER.get() {
        if let Ok(sender) = sender_mutex.lock() {
            let _ = sender.send(LogEntry::Message(formatted_message));
        }
    } else {
        // Fallback if not initialized
        println!("{}", formatted_message);
    }
}
