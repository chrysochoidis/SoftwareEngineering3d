package com.github.mikephil.charting.components;

import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.FSize;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the legend of the chart. The legend will contain one entry
 * per color and DataSet. Multiple colors in one DataSet are grouped together.
 * The legend object is NOT available before setting data to the chart.
 *
 * @author Philipp Jahoda
 */
public class Legend extends ComponentBase {

    public enum LegendForm {
        /**
         * Avoid drawing a form
         */
        NONE,

        /**
         * Do not draw the a form, but leave space for it
         */
        EMPTY,

        /**
         * Use default (default dataset's form to the legend's form)
         */
        DEFAULT,

        /**
         * Draw a square
         */
        SQUARE,

        /**
         * Draw a circle
         */
        CIRCLE,

        /**
         * Draw a horizontal line
         */
        LINE
    }

    public enum LegendHorizontalAlignment {
        LEFT, CENTER, RIGHT
    }

    public enum LegendVerticalAlignment {
        TOP, CENTER, BOTTOM
    }

    public enum LegendOrientation {
        HORIZONTAL, VERTICAL
    }

    public enum LegendDirection {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    /**
     * The legend entries array
     */
    private LegendEntry[] mEntries = new LegendEntry[]{};

    /**
     * Entries that will be appended to the end of the auto calculated entries after calculating the legend.
     * (if the legend has already been calculated, you will need to call notifyDataSetChanged() to let the changes take effect)
     */
    private LegendEntry[] mExtraEntries;

    /**
     * Are the legend labels/colors a custom value or auto calculated? If false,
     * then it's auto, if true, then custom. default false (automatic legend)
     */
    private boolean mIsLegendCustom = false;

    private LegendHorizontalAlignment mHorizontalAlignment = LegendHorizontalAlignment.LEFT;
    private LegendVerticalAlignment mVerticalAlignment = LegendVerticalAlignment.BOTTOM;
    private LegendOrientation mOrientation = LegendOrientation.HORIZONTAL;
    private boolean mDrawInside = false;

    /**
     * the text direction for the legend
     */
    private LegendDirection mDirection = LegendDirection.LEFT_TO_RIGHT;

    /**
     * the shape/form the legend colors are drawn in
     */
    private LegendForm mShape = LegendForm.SQUARE;

    /**
     * the size of the legend forms/shapes
     */
    private float mFormSize = 8f;

    /**
     * the size of the legend forms/shapes
     */
    private float mFormLineWidth = 3f;

    /**
     * Line dash path effect used for shapes that consist of lines.
     */
    private DashPathEffect mFormLineDashEffect = null;

    /**
     * the space between the legend entries on a horizontal axis, default 6f
     */
    private float mXEntrySpace = 6f;

    /**
     * the space between the legend entries on a vertical axis, default 5f
     */
    private float mYEntrySpace = 0f;

    /**
     * the space between the legend entries on a vertical axis, default 2f
     * private float mYEntrySpace = 2f; /** the space between the form and the
     * actual label/text
     */
    private float mFormToTextSpace = 5f;

    /**
     * the space that should be left between stacked forms
     */
    private float mStackSpace = 3f;

    /**
     * the maximum relative size out of the whole chart view in percent
     */
    private float mMaxSizePercent = 0.95f;

    /**
     * default constructor
     */
    public Legend() {

        this.mTextSize = Utils.convertDpToPixel(10f);
        this.mXOffset = Utils.convertDpToPixel(5f);
        this.mYOffset = Utils.convertDpToPixel(3f); // 2
    }

    /**
     * Constructor. Provide entries for the legend.
     *
     * @param entries
     */
    public Legend(LegendEntry[] entries) {
        this();

        if (entries == null) {
            throw new IllegalArgumentException("entries array is NULL");
        }

        this.mEntries = entries;
    }

    /**
     * This method sets the automatically computed colors for the legend. Use setCustom(...) to set custom colors.
     *
     * @param entries
     */
    public void setEntries(List<LegendEntry> entries) {
        mEntries = entries.toArray(new LegendEntry[entries.size()]);
    }

    public LegendEntry[] getEntries() {
        return mEntries;
    }

    /**
     * returns the maximum length in pixels across all legend labels + formsize
     * + formtotextspace
     *
     * @param p the paint object used for rendering the text
     * @return
     */
    public float getMaximumEntryWidth(Paint p) {

        float max = 0f;
        float maxFormSize = 0f;
        float formToTextSpace = Utils.convertDpToPixel(mFormToTextSpace);

        for (LegendEntry entry : mEntries) {
            final float formSize = Utils.convertDpToPixel(
                    Float.isNaN(entry.formSize)
                    ? mFormSize : entry.formSize);
            if (formSize > maxFormSize)
                maxFormSize = formSize;

            String label = entry.label;
            if (label == null) continue;

            float length = (float) Utils.calcTextWidth(p, label);

            if (length > max)
                max = length;
        }

        return max + maxFormSize + formToTextSpace;
    }

    /**
     * returns the maximum height in pixels across all legend labels
     *
     * @param p the paint object used for rendering the text
     * @return
     */
    public float getMaximumEntryHeight(Paint p) {

        float max = 0f;

        for (LegendEntry entry : mEntries) {
            String label = entry.label;
            if (label == null) continue;

            float length = (float) Utils.calcTextHeight(p, label);

            if (length > max)
                max = length;
        }

        return max;
    }

    public LegendEntry[] getExtraEntries() {

        return mExtraEntries;
    }

    public void setExtra(List<LegendEntry> entries) {
        mExtraEntries = entries.toArray(new LegendEntry[entries.size()]);
    }

    public void setExtra(LegendEntry[] entries) {
        if (entries == null)
            entries = new LegendEntry[]{};
        mExtraEntries = entries;
    }

    /**
     * Entries that will be appended to the end of the auto calculated
     *   entries after calculating the legend.
     * (if the legend has already been calculated, you will need to call notifyDataSetChanged()
     *   to let the changes take effect)
     */
    public void setExtra(int[] colors, String[] labels) {

        List<LegendEntry> entries = new ArrayList<>();

        for (int i = 0; i < Math.min(colors.length, labels.length); i++) {
            final LegendEntry entry = new LegendEntry();
            entry.formColor = colors[i];
            entry.label = labels[i];

            if (entry.formColor == ColorTemplate.COLOR_SKIP ||
                    entry.formColor == 0)
                entry.form = LegendForm.NONE;
            else if (entry.formColor == ColorTemplate.COLOR_NONE)
                entry.form = LegendForm.EMPTY;

            entries.add(entry);
        }

        mExtraEntries = entries.toArray(new LegendEntry[entries.size()]);
    }

    /**
     * Sets a custom legend's entries array.
     * * A null label will start a group.
     * This will disable the feature that automatically calculates the legend
     *   entries from the datasets.
     * Call resetCustom() to re-enable automatic calculation (and then
     *   notifyDataSetChanged() is needed to auto-calculate the legend again)
     */
    public void setCustom(LegendEntry[] entries) {

        mEntries = entries;
        mIsLegendCustom = true;
    }

    /**
     * Sets a custom legend's entries array.
     * * A null label will start a group.
     * This will disable the feature that automatically calculates the legend
     *   entries from the datasets.
     * Call resetCustom() to re-enable automatic calculation (and then
     *   notifyDataSetChanged() is needed to auto-calculate the legend again)
     */
    public void setCustom(List<LegendEntry> entries) {

        mEntries = entries.toArray(new LegendEntry[entries.size()]);
        mIsLegendCustom = true;
    }

    /**
     * Calling this will disable the custom legend entries (set by
     * setCustom(...)). Instead, the entries will again be calculated
     * automatically (after notifyDataSetChanged() is called).
     */
    public void resetCustom() {
        mIsLegendCustom = false;
    }

    /**
     * @return true if a custom legend entries has been set default
     * false (automatic legend)
     */
    public boolean isLegendCustom() {
        return mIsLegendCustom;
    }

    /**
     * returns the horizontal alignment of the legend
     *
     * @return
     */
    public LegendHorizontalAlignment getHorizontalAlignment() {
        return mHorizontalAlignment;
    }

    /**
     * sets the horizontal alignment of the legend
     *
     * @param value
     */
    public void setHorizontalAlignment(LegendHorizontalAlignment value) {
        mHorizontalAlignment = value;
    }

    /**
     * returns the vertical alignment of the legend
     *
     * @return
     */
    public LegendVerticalAlignment getVerticalAlignment() {
        return mVerticalAlignment;
    }

    /**
     * sets the vertical alignment of the legend
     *
     * @param value
     */
    public void setVerticalAlignment(LegendVerticalAlignment value) {
        mVerticalAlignment = value;
    }

    /**
     * returns the orientation of the legend
     *
     * @return
     */
    public LegendOrientation getOrientation() {
        return mOrientation;
    }

    /**
     * sets the orientation of the legend
     *
     * @param value
     */
    public void setOrientation(LegendOrientation value) {
        mOrientation = value;
    }

    /**
     * returns whether the legend will draw inside the chart or outside
     *
     * @return
     */
    public boolean isDrawInsideEnabled() {
        return mDrawInside;
    }

    /**
     * sets whether the legend will draw inside the chart or outside
     *
     * @param value
     */
    public void setDrawInside(boolean value) {
        mDrawInside = value;
    }

    /**
     * returns the text direction of the legend
     *
     * @return
     */
    public LegendDirection getDirection() {
        return mDirection;
    }

    /**
     * sets the text direction of the legend
     *
     * @param pos
     */
    public void setDirection(LegendDirection pos) {
        mDirection = pos;
    }

    /**
     * returns the current form/shape that is set for the legend
     *
     * @return
     */
    public LegendForm getForm() {
        return mShape;
    }

    /**
     * sets the form/shape of the legend forms
     *
     * @param shape
     */
    public void setForm(LegendForm shape) {
        mShape = shape;
    }

    /**
     * sets the size in dp of the legend forms, default 8f
     *
     * @param size
     */
    public void setFormSize(float size) {
        mFormSize = size;
    }

    /**
     * returns the size in dp of the legend forms
     *
     * @return
     */
    public float getFormSize() {
        return mFormSize;
    }

    /**
     * sets the line width in dp for forms that consist of lines, default 3f
     *
     * @param size
     */
    public void setFormLineWidth(float size) {
        mFormLineWidth = size;
    }

    /**
     * returns the line width in dp for drawing forms that consist of lines
     *
     * @return
     */
    public float getFormLineWidth() {
        return mFormLineWidth;
    }

    /**
     * Sets the line dash path effect used for shapes that consist of lines.
     *
     * @param dashPathEffect
     */
    public void setFormLineDashEffect(DashPathEffect dashPathEffect) {
        mFormLineDashEffect = dashPathEffect;
    }

    /**
     * @return The line dash path effect used for shapes that consist of lines.
     */
    public DashPathEffect getFormLineDashEffect() {
        return mFormLineDashEffect;
    }

    /**
     * returns the space between the legend entries on a horizontal axis in
     * pixels
     *
     * @return
     */
    public float getXEntrySpace() {
        return mXEntrySpace;
    }

    /**
     * sets the space between the legend entries on a horizontal axis in pixels,
     * converts to dp internally
     *
     * @param space
     */
    public void setXEntrySpace(float space) {
        mXEntrySpace = space;
    }

    /**
     * returns the space between the legend entries on a vertical axis in pixels
     *
     * @return
     */
    public float getYEntrySpace() {
        return mYEntrySpace;
    }

    /**
     * sets the space between the legend entries on a vertical axis in pixels,
     * converts to dp internally
     *
     * @param space
     */
    public void setYEntrySpace(float space) {
        mYEntrySpace = space;
    }

    /**
     * returns the space between the form and the actual label/text
     *
     * @return
     */
    public float getFormToTextSpace() {
        return mFormToTextSpace;
    }

    /**
     * sets the space between the form and the actual label/text, converts to dp
     * internally
     *
     * @param space
     */
    public void setFormToTextSpace(float space) {
        this.mFormToTextSpace = space;
    }

    /**
     * returns the space that is left out between stacked forms (with no label)
     *
     * @return
     */
    public float getStackSpace() {
        return mStackSpace;
    }

    /**
     * sets the space that is left out between stacked forms (with no label)
     *
     * @param space
     */
    public void setStackSpace(float space) {
        mStackSpace = space;
    }

    /**
     * the total width of the legend (needed width space)
     */
    public float mNeededWidth = 0f;

    /**
     * the total height of the legend (needed height space)
     */
    public float mNeededHeight = 0f;

    public float mTextHeightMax = 0f;

    public float mTextWidthMax = 0f;

    /**
     * flag that indicates if word wrapping is enabled
     */
    private boolean mWordWrapEnabled = false;

    /**
     * Should the legend word wrap? / this is currently supported only for:
     * BelowChartLeft, BelowChartRight, BelowChartCenter. / note that word
     * wrapping a legend takes a toll on performance. / you may want to set
     * maxSizePercent when word wrapping, to set the point where the text wraps.
     * / default: false
     *
     * @param enabled
     */
    public void setWordWrapEnabled(boolean enabled) {
        mWordWrapEnabled = enabled;
    }

    /**
     * If this is set, then word wrapping the legend is enabled. This means the
     * legend will not be cut off if too long.
     *
     * @return
     */
    public boolean isWordWrapEnabled() {
        return mWordWrapEnabled;
    }

    /**
     * The maximum relative size out of the whole chart view. / If the legend is
     * to the right/left of the chart, then this affects the width of the
     * legend. / If the legend is to the top/bottom of the chart, then this
     * affects the height of the legend. / If the legend is the center of the
     * piechart, then this defines the size of the rectangular bounds out of the
     * size of the "hole". / default: 0.95f (95%)
     *
     * @return
     */
    public float getMaxSizePercent() {
        return mMaxSizePercent;
    }

    /**
     * The maximum relative size out of the whole chart view. / If
     * the legend is to the right/left of the chart, then this affects the width
     * of the legend. / If the legend is to the top/bottom of the chart, then
     * this affects the height of the legend. / default: 0.95f (95%)
     *
     * @param maxSize
     */
    public void setMaxSizePercent(float maxSize) {
        mMaxSizePercent = maxSize;
    }

    private List<FSize> mCalculatedLabelSizes = new ArrayList<>(16);
    private List<Boolean> mCalculatedLabelBreakPoints = new ArrayList<>(16);
    private List<FSize> mCalculatedLineSizes = new ArrayList<>(16);

    public List<FSize> getCalculatedLabelSizes() {
        return mCalculatedLabelSizes;
    }

    public List<Boolean> getCalculatedLabelBreakPoints() {
        return mCalculatedLabelBreakPoints;
    }

    public List<FSize> getCalculatedLineSizes() {
        return mCalculatedLineSizes;
    }

    /**
     * Calculates the dimensions of the Legend. This includes the maximum width
     * and height of a single entry, as well as the total width and height of
     * the Legend.
     *
     * @param labelpaint
     */
    public void calculateDimensions(Paint labelPaint, ViewPortHandler viewPortHandler) {
        final float defaultFormSize = Utils.convertDpToPixel(mFormSize);
        final float stackSpace = Utils.convertDpToPixel(mStackSpace);
        final float formToTextSpace = Utils.convertDpToPixel(mFormToTextSpace);
        final float xEntrySpace = Utils.convertDpToPixel(mXEntrySpace);
        final float yEntrySpace = Utils.convertDpToPixel(mYEntrySpace);

        mTextWidthMax = getMaximumEntryWidth(labelPaint);
        mTextHeightMax = getMaximumEntryHeight(labelPaint);

        if (mOrientation == LegendOrientation.VERTICAL) {
            calculateVerticalDimensions(labelPaint, stackSpace, formToTextSpace, yEntrySpace, defaultFormSize);
        } else if (mOrientation == LegendOrientation.HORIZONTAL) {
            calculateHorizontalDimensions(labelPaint, viewPortHandler, stackSpace, formToTextSpace, xEntrySpace, yEntrySpace, defaultFormSize);
        }

        mNeededHeight += mYOffset;
        mNeededWidth += mXOffset;
    }

    private void calculateVerticalDimensions(Paint labelPaint, float stackSpace, float formToTextSpace,
                                             float yEntrySpace, float defaultFormSize) {
        float maxWidth = 0f, maxHeight = 0f, lineWidth = 0f;
        final float labelLineHeight = Utils.getLineHeight(labelPaint);
        boolean wasStacked = false;

        for (LegendEntry entry : mEntries) {
            boolean hasForm = entry.form != LegendForm.NONE;
            float formSize = Float.isNaN(entry.formSize) ? defaultFormSize : Utils.convertDpToPixel(entry.formSize);
            String label = entry.label;

            if (!wasStacked) lineWidth = 0f;

            if (hasForm) {
                if (wasStacked) lineWidth += stackSpace;
                lineWidth += formSize;
            }

            if (label != null) {
                if (hasForm && !wasStacked) lineWidth += formToTextSpace;
                else if (wasStacked) {
                    maxWidth = Math.max(maxWidth, lineWidth);
                    maxHeight += labelLineHeight + yEntrySpace;
                    lineWidth = 0f;
                    wasStacked = false;
                }
                lineWidth += Utils.calcTextWidth(labelPaint, label);
                if (isNotLastEntry(entry)) maxHeight += labelLineHeight + yEntrySpace;
            } else {
                wasStacked = true;
                lineWidth += formSize + stackSpace;
            }

            maxWidth = Math.max(maxWidth, lineWidth);
        }

        mNeededWidth = maxWidth;
        mNeededHeight = maxHeight;
    }

    private void calculateHorizontalDimensions(Paint labelPaint, ViewPortHandler viewPortHandler, float stackSpace,
                                               float formToTextSpace, float xEntrySpace, float yEntrySpace, float defaultFormSize) {
        final float labelLineHeight = Utils.getLineHeight(labelPaint);
        final float labelLineSpacing = Utils.getLineSpacing(labelPaint) + yEntrySpace;
        final float contentWidth = viewPortHandler.contentWidth() * mMaxSizePercent;

        float maxLineWidth = 0f, currentLineWidth = 0f, requiredWidth = 0f;
        int stackedStartIndex = -1;

        mCalculatedLabelBreakPoints.clear();
        mCalculatedLabelSizes.clear();
        mCalculatedLineSizes.clear();

        for (int i = 0; i < mEntries.length; i++) {
            LegendEntry entry = mEntries[i];
            boolean hasForm = entry.form != LegendForm.NONE;
            float formSize = Float.isNaN(entry.formSize) ? defaultFormSize : Utils.convertDpToPixel(entry.formSize);
            String label = entry.label;

            mCalculatedLabelBreakPoints.add(false);
            requiredWidth = (stackedStartIndex == -1) ? 0f : requiredWidth + stackSpace;

            if (label != null) {
                mCalculatedLabelSizes.add(Utils.calcTextSize(labelPaint, label));
                requiredWidth += (hasForm ? formToTextSpace + formSize : 0f) + mCalculatedLabelSizes.get(i).width;
            } else {
                mCalculatedLabelSizes.add(FSize.getInstance(0f, 0f));
                requiredWidth += (hasForm ? formSize : 0f);
                if (stackedStartIndex == -1) stackedStartIndex = i;
            }

            if (label != null || i == mEntries.length - 1) {
                float requiredSpacing = (currentLineWidth == 0f) ? 0f : xEntrySpace;
                if (!mWordWrapEnabled || currentLineWidth == 0f || contentWidth - currentLineWidth >= requiredSpacing + requiredWidth) {
                    currentLineWidth += requiredSpacing + requiredWidth;
                } else {
                    mCalculatedLineSizes.add(FSize.getInstance(currentLineWidth, labelLineHeight));
                    maxLineWidth = Math.max(maxLineWidth, currentLineWidth);
                    mCalculatedLabelBreakPoints.set((stackedStartIndex > -1) ? stackedStartIndex : i, true);
                    currentLineWidth = requiredWidth;
                }

                if (i == mEntries.length - 1) {
                    mCalculatedLineSizes.add(FSize.getInstance(currentLineWidth, labelLineHeight));
                    maxLineWidth = Math.max(maxLineWidth, currentLineWidth);
                }
            }

            stackedStartIndex = (label != null) ? -1 : stackedStartIndex;
        }

        mNeededWidth = maxLineWidth;
        mNeededHeight = labelLineHeight * mCalculatedLineSizes.size() + labelLineSpacing * (mCalculatedLineSizes.size() - 1);
    }

    private boolean isNotLastEntry(LegendEntry entry) {
        return entry != mEntries[mEntries.length - 1];
    }
}
