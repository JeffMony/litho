/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.litho;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static androidx.core.view.ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
import static androidx.core.view.ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO;
import static androidx.core.view.ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES;
import static com.facebook.litho.LithoMountData.getMountData;
import static org.assertj.core.api.Java6Assertions.assertThat;

import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.rendercore.MountItem;
import com.facebook.rendercore.RenderTreeNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

/** Tests {@link MountItem} */
@RunWith(ComponentsTestRunner.class)
public class MountItemTest {
  private MountItem mMountItem;
  private Component mComponent;
  private ComponentHost mComponentHost;
  private Object mContent;
  private CharSequence mContentDescription;
  private Object mViewTag;
  private SparseArray<Object> mViewTags;
  private EventHandler mClickHandler;
  private EventHandler mLongClickHandler;
  private EventHandler mFocusChangeHandler;
  private EventHandler mTouchHandler;
  private EventHandler mDispatchPopulateAccessibilityEventHandler;
  private int mFlags;
  private ComponentContext mContext;
  private NodeInfo mNodeInfo;

  @Before
  public void setup() throws Exception {
    mContext = new ComponentContext(RuntimeEnvironment.application);

    mComponent =
        new InlineLayoutSpec() {
          @Override
          protected Component onCreateLayout(ComponentContext c) {
            return TestDrawableComponent.create(c).build();
          }
        };
    mComponentHost = new ComponentHost(RuntimeEnvironment.application);
    mContent = new View(RuntimeEnvironment.application);
    mContentDescription = "contentDescription";
    mViewTag = "tag";
    mViewTags = new SparseArray<>();
    mClickHandler = new EventHandler(mComponent, 5);
    mLongClickHandler = new EventHandler(mComponent, 3);
    mFocusChangeHandler = new EventHandler(mComponent, 9);
    mTouchHandler = new EventHandler(mComponent, 1);
    mDispatchPopulateAccessibilityEventHandler = new EventHandler(mComponent, 7);
    mFlags = 114;

    mNodeInfo = new DefaultNodeInfo();
    mNodeInfo.setContentDescription(mContentDescription);
    mNodeInfo.setClickHandler(mClickHandler);
    mNodeInfo.setLongClickHandler(mLongClickHandler);
    mNodeInfo.setFocusChangeHandler(mFocusChangeHandler);
    mNodeInfo.setTouchHandler(mTouchHandler);
    mNodeInfo.setViewTag(mViewTag);
    mNodeInfo.setViewTags(mViewTags);
    mMountItem = create(mContent);
  }

  MountItem create(Object content) {
    return MountItemTestHelper.create(
        mComponent,
        mComponentHost,
        content,
        mNodeInfo,
        null,
        null,
        0,
        0,
        mFlags,
        0,
        IMPORTANT_FOR_ACCESSIBILITY_YES,
        ORIENTATION_PORTRAIT,
        null);
  }

  @Test
  public void testIsBound() {
    mMountItem.setIsBound(true);
    assertThat(mMountItem.isBound()).isTrue();

    mMountItem.setIsBound(false);
    assertThat(mMountItem.isBound()).isFalse();
  }

  @Test
  public void testGetters() {
    assertThat(getMountData(mMountItem).getComponent()).isSameAs((Component) mComponent);
    assertThat(mMountItem.getHost()).isSameAs(mComponentHost);
    assertThat(mMountItem.getContent()).isSameAs(mContent);
    assertThat(getMountData(mMountItem).getNodeInfo().getContentDescription())
        .isSameAs(mContentDescription);
    assertThat(getMountData(mMountItem).getNodeInfo().getClickHandler()).isSameAs(mClickHandler);
    assertThat(getMountData(mMountItem).getNodeInfo().getFocusChangeHandler())
        .isSameAs(mFocusChangeHandler);
    assertThat(getMountData(mMountItem).getNodeInfo().getTouchHandler()).isSameAs(mTouchHandler);
    assertThat(getMountData(mMountItem).getLayoutFlags()).isEqualTo(mFlags);
    assertThat(getMountData(mMountItem).getImportantForAccessibility())
        .isEqualTo(IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  @Test
  public void testFlags() {
    mFlags =
        LithoMountData.LAYOUT_FLAG_DUPLICATE_PARENT_STATE
            | LithoMountData.LAYOUT_FLAG_DISABLE_TOUCHABLE;

    mMountItem = create(mContent);

    assertThat(LithoMountData.isDuplicateParentState(getMountData(mMountItem).getLayoutFlags()))
        .isTrue();
    assertThat(LithoMountData.isTouchableDisabled(getMountData(mMountItem).getLayoutFlags()))
        .isTrue();

    mFlags = 0;

    mMountItem = create(mContent);

    assertThat(LithoMountData.isDuplicateParentState(getMountData(mMountItem).getLayoutFlags()))
        .isFalse();
    assertThat(LithoMountData.isTouchableDisabled(getMountData(mMountItem).getLayoutFlags()))
        .isFalse();
  }

  @Test
  public void testViewFlags() {
    View view = new View(RuntimeEnvironment.application);
    view.setClickable(true);
    view.setEnabled(true);
    view.setLongClickable(true);
    view.setFocusable(false);
    view.setSelected(false);

    mMountItem = create(view);

    assertThat(getMountData(mMountItem).isViewClickable()).isTrue();
    assertThat(getMountData(mMountItem).isViewEnabled()).isTrue();
    assertThat(getMountData(mMountItem).isViewLongClickable()).isTrue();
    assertThat(getMountData(mMountItem).isViewFocusable()).isFalse();
    assertThat(getMountData(mMountItem).isViewSelected()).isFalse();

    view.setClickable(false);
    view.setEnabled(false);
    view.setLongClickable(false);
    view.setFocusable(true);
    view.setSelected(true);

    mMountItem = create(view);

    assertThat(getMountData(mMountItem).isViewClickable()).isFalse();
    assertThat(getMountData(mMountItem).isViewEnabled()).isFalse();
    assertThat(getMountData(mMountItem).isViewLongClickable()).isFalse();
    assertThat(getMountData(mMountItem).isViewFocusable()).isTrue();
    assertThat(getMountData(mMountItem).isViewSelected()).isTrue();
  }

  @Test
  public void testIsAccessibleWithNullComponent() {
    final MountItem mountItem = create(mContent);

    assertThat(getMountData(mountItem).isAccessible()).isFalse();
  }

  @Test
  public void testIsAccessibleWithAccessibleComponent() {
    final MountItem mountItem =
        MountItemTestHelper.create(
            TestDrawableComponent.create(
                    mContext, true, true, true, true /* implementsAccessibility */)
                .build(),
            mComponentHost,
            mContent,
            mNodeInfo,
            null,
            null,
            mFlags,
            0,
            0,
            0,
            IMPORTANT_FOR_ACCESSIBILITY_AUTO,
            ORIENTATION_PORTRAIT,
            null);

    assertThat(getMountData(mountItem).isAccessible()).isTrue();
  }

  @Test
  public void testIsAccessibleWithDisabledAccessibleComponent() {
    final MountItem mountItem =
        MountItemTestHelper.create(
            TestDrawableComponent.create(
                    mContext, true, true, true, true /* implementsAccessibility */)
                .build(),
            mComponentHost,
            mContent,
            mNodeInfo,
            null,
            null,
            mFlags,
            0,
            0,
            0,
            IMPORTANT_FOR_ACCESSIBILITY_NO,
            ORIENTATION_PORTRAIT,
            null);

    assertThat(getMountData(mountItem).isAccessible()).isFalse();
  }

  @Test
  public void testIsAccessibleWithAccessibilityEventHandler() {
    final MountItem mountItem =
        MountItemTestHelper.create(
            TestDrawableComponent.create(
                    mContext, true, true, true, true /* implementsAccessibility */)
                .build(),
            mComponentHost,
            mContent,
            mNodeInfo,
            null,
            null,
            mFlags,
            0,
            0,
            0,
            IMPORTANT_FOR_ACCESSIBILITY_AUTO,
            ORIENTATION_PORTRAIT,
            null);

    assertThat(getMountData(mountItem).isAccessible()).isTrue();
  }

  @Test
  public void testIsAccessibleWithNonAccessibleComponent() {
    assertThat(getMountData(mMountItem).isAccessible()).isFalse();
  }

  @Test
  public void testUpdateDoesntChangeFlags() {
    LayoutOutput output =
        new LayoutOutput(mNodeInfo, null, mComponent, new Rect(0, 0, 0, 0), 0, 0, 0, 0, 0, 0, null);
    RenderTreeNode node = LayoutOutput.create(output, null);

    View view = new View(RuntimeEnvironment.application);

    final LithoMountData data = new LithoMountData(output, view);
    final MountItem mountItem = new MountItem(node, mComponentHost, view);
    mountItem.setMountData(data);

    assertThat(getMountData(mountItem).isViewClickable()).isFalse();

    view.setClickable(true);

    mountItem.update(node);
    assertThat(getMountData(mountItem).isViewClickable()).isFalse();
  }
}
