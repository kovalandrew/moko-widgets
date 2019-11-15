/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.widgets.units

import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.units.CollectionUnitItem
import dev.icerock.moko.widgets.core.Widget
import kotlinx.cinterop.cstr
import platform.UIKit.UIApplication
import platform.UIKit.UICollectionView
import platform.UIKit.UICollectionViewCell
import platform.UIKit.addSubview
import platform.UIKit.bottomAnchor
import platform.UIKit.leftAnchor
import platform.UIKit.rightAnchor
import platform.UIKit.topAnchor
import platform.objc.OBJC_ASSOCIATION_RETAIN
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject

actual abstract class WidgetsCollectionUnitItem<T> actual constructor(
    override val itemId: Long,
    private val data: T
) : CollectionUnitItem {
    actual abstract val reuseId: String
    actual abstract fun createWidget(data: LiveData<T>): Widget

    override val reusableIdentifier: String get() = reuseId

    override fun register(intoView: UICollectionView) {
        intoView.registerClass(
            cellClass = UICollectionViewCell().`class`(),
            forCellWithReuseIdentifier = reusableIdentifier
        )
    }

    override fun bind(cell: UICollectionViewCell) {
        val liveData = cell.getWidgetLiveData<T>()
        if (liveData != null) {
            liveData.value = data
        } else {
            val viewController = UIApplication.sharedApplication.keyWindow?.rootViewController!!
            val mutableLiveData = MutableLiveData(initialValue = data)
            val widget = createWidget(mutableLiveData)
            val view = widget.buildView(viewFactoryContext = viewController)

            with(cell.contentView) {
                addSubview(view)

                view.topAnchor.constraintEqualToAnchor(topAnchor).active = true
                view.leftAnchor.constraintEqualToAnchor(leftAnchor).active = true
                view.rightAnchor.constraintEqualToAnchor(rightAnchor).active = true
                view.bottomAnchor.constraintEqualToAnchor(bottomAnchor).active = true
            }

            cell.setWidgetLiveData(mutableLiveData)
        }
    }
}

private const val widgetLiveDataKey = "widgetLiveData"

private fun <T> UICollectionViewCell.getWidgetLiveData(): MutableLiveData<T>? {
    return objc_getAssociatedObject(
        `object` = this,
        key = widgetLiveDataKey.cstr
    ) as? MutableLiveData<T>
}

private fun <T> UICollectionViewCell.setWidgetLiveData(liveData: MutableLiveData<T>) {
    objc_setAssociatedObject(
        `object` = this,
        key = widgetLiveDataKey.cstr,
        value = liveData,
        policy = OBJC_ASSOCIATION_RETAIN
    )
}
