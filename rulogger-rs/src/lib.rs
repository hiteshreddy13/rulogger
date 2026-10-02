use jni::objects::{JClass, JString};
use jni::JNIEnv;
use std::time::{SystemTime, UNIX_EPOCH};

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

    // Very simple and fast format for demonstration
    println!("[{}] [{}] {}", now, level, message);
}
