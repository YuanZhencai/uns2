/**
 * Application.java
 * Created: 2011-11-29 上午2:12:00
 */
package com.wcs.uns;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import com.wcs.uns.service.MsgResource;

/**
 * <p>Project: uns</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2011 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:shenbo@wcs-global.com">Shen Bo</a>
 */
@ApplicationPath("rs")
public class Application extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
       Set<Class<?>> s = new HashSet<Class<?>>();
       s.add(MsgResource.class);
       return s;
    }

}
