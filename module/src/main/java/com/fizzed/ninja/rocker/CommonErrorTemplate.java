package com.fizzed.ninja.rocker;

import ninja.utils.Message;

/**
 *
 * @author joelauer
 */
public interface CommonErrorTemplate {
    
    <T> T message(Message message);
    
}
