package com.example.myweibo

import com.example.myweibo.data.shoppingPackage
import com.example.myweibo.data.isWeiboShoppingCard
import com.example.myweibo.data.weiboShoppingDestination
import org.junit.Assert.*
import org.junit.Test

class ShoppingAppLinksTest {
    @Test fun cardWrapperUsesActualGoodsDestination() {
        assertEquals("https://shop.sc.weibo.com/h5/goods/index?iid=110031382798710004226584",
            weiboShoppingDestination("http://apps.weibo.com/linkcard/2017845002:shop_sc_weibo_110031382798710004226584"))
        assertNull(weiboShoppingDestination("https://apps.weibo.com/linkcard/2017845002:shop_sc_weibo_invalid"))
    }
    @Test fun authenticatedWeiboCardsAreNotMistakenForDirectShoppingLinks() {
        assertTrue(isWeiboShoppingCard("http://apps.weibo.com/linkcard/2017845002:shop_sc_weibo_110031382798710004226584"))
        assertTrue(isWeiboShoppingCard("https://shop.sc.weibo.com/h5/goods/index?iid=123"))
        assertFalse(isWeiboShoppingCard("https://apps.weibo.com.evil.test/linkcard/2017845002:shop_sc_weibo_123"))
        assertFalse(isWeiboShoppingCard("https://apps.weibo.com/linkcard/other"))
        assertFalse(isWeiboShoppingCard("javascript://apps.weibo.com/linkcard/2017845002:shop_sc_weibo_123"))
    }
    @Test fun shoppingDomainsAndSchemesUseOfficialPackages() {
        assertEquals("com.taobao.taobao", shoppingPackage("https", "item.taobao.com"))
        assertEquals("com.taobao.taobao", shoppingPackage("https", "e.tb.cn"))
        assertEquals("com.jingdong.app.mall", shoppingPackage("https", "item.jd.com"))
        assertEquals("com.jingdong.app.mall", shoppingPackage("openapp.jdmobile", "virtual"))
        assertEquals("com.tmall.wireless", shoppingPackage("https", "detail.tmall.com"))
    }

    @Test fun untrustedHostsAndLocalSchemesAreNotLaunched() {
        assertNull(shoppingPackage("https", "jd.com.evil.example"))
        assertNull(shoppingPackage("https", "nottaobao.com"))
        assertNull(shoppingPackage("file", "jd.com"))
        assertNull(shoppingPackage("content", "taobao.com"))
        assertNull(shoppingPackage("javascript", "jd.com"))
    }
}
